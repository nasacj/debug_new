/*
 * debug_new.cpp  1.11 2003/07/03
 *
 * Implementation of debug versions of new and delete to check leakage
 *
 *
 */

#include <new>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <stdarg.h>
#include <string.h>
#ifdef WIN32
#include <Windows.h>
#include <process.h>
#else
#include <pthread.h>
#include <sys/types.h>
#include <sys/inotify.h>
#include <errno.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <sys/wait.h>
#endif
//#include "avl.h"


#ifdef _MSC_VER
#pragma warning(disable: 4073)
#pragma init_seg(lib)
#endif

#ifndef DEBUG_NEW_HASHTABLESIZE
//#define DEBUG_NEW_HASHTABLESIZE 16384
#define DEBUG_NEW_HASHTABLESIZE 0x40
#endif

#ifndef DEBUG_NEW_HASH
#define DEBUG_NEW_HASH(p) (((unsigned)(p) >> 8) % DEBUG_NEW_HASHTABLESIZE)
#endif

// The default behaviour now is to copy the file name, because we found
// that the exit leakage check cannot access the address of the file
// name sometimes (in our case, a core dump will occur when trying to
// access the file name in a shared library after a SIGINT).
#ifndef DEBUG_NEW_FILENAME_LEN
#define DEBUG_NEW_FILENAME_LEN	256
#endif
#if DEBUG_NEW_FILENAME_LEN == 0 && !defined(DEBUG_NEW_NO_FILENAME_COPY)
#define DEBUG_NEW_NO_FILENAME_COPY
#endif
#ifndef DEBUG_NEW_NO_FILENAME_COPY
#include <string.h>
#endif



struct new_ptr_list_t
{
	new_ptr_list_t*		next;
#ifdef DEBUG_NEW_NO_FILENAME_COPY
	const char*			file;
#else
	char					file[DEBUG_NEW_FILENAME_LEN];
#endif
	int					line;
	size_t				size;
	void*				memory;
	void*				caller_add;
};

struct prt_in_avl_table
{
	new_ptr_list_t* ptr;
	int repeatNum;
	prt_in_avl_table* next;
};

static new_ptr_list_t* new_ptr_list[DEBUG_NEW_HASHTABLESIZE] = {0};

bool new_autocheck_flag = true;
bool isPrint = false;
bool isRecord = true;
bool isPrintUnknow = true;

#ifdef WIN32
CRITICAL_SECTION global_lock;
#else
pthread_mutex_t global_lock;
#endif

void initial_locker()
{
#ifdef WIN32
	InitializeCriticalSection(&global_lock);
#else
	pthread_mutex_init(&global_lock, NULL);
#endif
}

void thread_lock()
{
#ifdef WIN32
	EnterCriticalSection(&global_lock);
#else
	pthread_mutex_lock(&global_lock);
#endif
}

void thread_unlock()
{
#ifdef WIN32
	LeaveCriticalSection(&global_lock);
#else
	pthread_mutex_unlock(&global_lock);
#endif
}

void uninitial_locker()
{
#ifdef WIN32
	DeleteCriticalSection(&global_lock);
#else
	pthread_mutex_destroy(&global_lock);
#endif
}

class Locker
{
public:
	Locker(){thread_lock();}
	~Locker(){thread_unlock();}
};

bool get_isRecode()
{
	Locker lock;
	return isRecord;
}

void set_isRecode(bool _is)
{
	Locker lock;
	isRecord = _is;
}

FILE* logFile = NULL;
FILE* outFile = NULL;
void initialLog(FILE* file)
{
	logFile = file;
}

void initialOutFile(FILE* file)
{
	outFile = file;
}

void uninitialLog()
{
	if (logFile != NULL)
	{
		fclose(logFile);
		logFile = NULL;
	}
	
}

void uninitialOutFile()
{
	if (outFile != NULL)
	{
		fclose(outFile);
		outFile = NULL;
	}
}

#ifdef WIN32
unsigned int WINAPI thread_file_monitor(void * tid);
#else
void* thread_file_monitor(void* parm);
#endif

void initial()
{
	initial_locker();

	FILE* log = fopen("/tmp/memory_check_log.txt", "ab+");
	//initialLog(stdout);
	initialLog(log);
	FILE* outf = fopen("/tmp/memory_allocate_log.txt", "ab+");
	initialOutFile(outf);

#ifdef WIN32
	unsigned int thread_id;
	uintptr_t thread_monit = _beginthreadex(NULL, 0, thread_file_monitor, NULL, 0, &thread_id);
#else
	pthread_t thread_monit;
	pthread_attr_t attr;
	pthread_attr_init(&attr);
	pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);
	pthread_create(&thread_monit, &attr, thread_file_monitor, NULL);
#endif
	
}

void uninitial()
{
	uninitial_locker();
	uninitialLog();
	uninitialOutFile();
}

int string_hash(char* stringLine)
{
	int h = 0;
	if (stringLine != NULL)
	{
		if (h == 0) {
			int off = 0;
			char * val = stringLine;
			int len = strlen(stringLine);

			for (int i = 0; i < len; i++) {
				h = 31*h + val[off++];
			}
		}
	}
	return h;
}

void start_record()
{
	set_isRecode(true);
}

void clean_record()
{
	Locker lock;
	
	for (int i = 0; i < DEBUG_NEW_HASHTABLESIZE; ++i)
	{
		new_ptr_list_t* ptr = new_ptr_list[i];
		if (ptr == NULL)
			continue;
		while (ptr)
		{
			new_ptr_list_t* ptr_last = ptr;
			ptr = ptr->next;
			free(ptr_last);
			if (ptr_last ==  new_ptr_list[i])
			{
				new_ptr_list[i] = NULL;
			}
			
		}
	}
}
void stop_record()
{
	if (!get_isRecode())
	{
		return;
	}
	set_isRecode(false);
	clean_record();
}


/*************************************************** Don't implement collecting where leaked in the same line code
libavl_allocator* avlAllocator = NULL;
avl_table * AVL_Table = NULL;
int comparer(const void *avl_a, const void *avl_b, void *avl_param)
{
	return (int)avl_a - (int)avl_b;
}

void destory (void *avl_item, void *avl_param)
{

	while (avl_item != 0)
	{
	}
}


void create_record()
{
	avlAllocator = (libavl_allocator *)malloc(sizeof(libavl_allocator));
	avlAllocator->libavl_malloc = avl_malloc;
	avlAllocator->libavl_free = avl_free;
	AVL_Table = avl_create(comparer, NULL, avlAllocator);
}

void insert_in_recode(void * item)
{
	prt_in_avl_table* ptr_in = avl_find(AVL_Table, item);
	if (ptr_in == NULL)
	{
		ptr_in = avl_insert(AVL_Table, item);
	}
	ptr_in->repeatNum ++;
	
}

void trace_record()
{
	for (int i = 0; i < DEBUG_NEW_HASHTABLESIZE; ++i)
	{
		new_ptr_list_t* ptr = new_ptr_list[i];
		if (ptr == NULL)
			continue;

		while (ptr)
		{
			avl_insert (AVL_Table, ptr);
			ptr = ptr->next;
		}
	}

}
*****************************************************************/

void printLog(FILE* file, const char* format, ...)
{
	va_list arg_list;
	char msg[2048];

	va_start(arg_list, format);
	vsprintf(msg, format, arg_list);
	va_end(arg_list);

	char *wday[]={"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	time_t timep;
	struct tm *p;
	time(&timep);
	p = localtime(&timep); /*取得当地时间*/

	fprintf(file, "[%d/%.2d/%.2d %s %.2d:%.2d:%.2d] ", 
		(1900+p->tm_year),( 1+p->tm_mon), p->tm_mday, wday[p->tm_wday],p->tm_hour, p->tm_min, p->tm_sec);
	fprintf(file, msg);
	fflush(file);
}

bool check_leaks()
{
	thread_lock();

	printLog(logFile, "Check Leaks ===================================================>\n");

	bool fLeaked = false;
	for (int i = 0; i < DEBUG_NEW_HASHTABLESIZE; ++i)
	{
		new_ptr_list_t* ptr = new_ptr_list[i];
		if (ptr == NULL)
			continue;
        //printf("ptr ---> %p (size %u, %s:%d)\n", ptr->memory, ptr->size, ptr->file, ptr->line);
        //printf("isPrintUnkonw = %d\n", isPrintUnknow);
		fLeaked = true;
		while (ptr)
		{
            if(true == isPrintUnknow || 0 != ptr->line)
			{
				fprintf(logFile, "%p ===> Memory Stay ------> %p (size %3u, %s:%-4d)\n",
						ptr->caller_add,
						(char*)ptr->memory,
						ptr->size,
						ptr->file,
						ptr->line);
			}
            ptr = ptr->next;
		}
	}
    printLog(logFile, "---------------------------\n");

	fflush(logFile);

	thread_unlock();

	if (fLeaked)
		return true;
	else
		return false;
}

void* record_new(size_t size, const char* file, int line, void* add)
{
    if(0 == size)
        return NULL;

	thread_lock();
	
	new_ptr_list_t* ptr = (new_ptr_list_t*)malloc(sizeof(new_ptr_list_t));
	void* memory_ptr = malloc(size);
	if (ptr == NULL || NULL == memory_ptr)
	{
		printLog(outFile, "new: !!!ERROR!!! out of memory when allocating %u bytes\n", size);
		abort();
        //while(1);
	}
	ptr->memory = memory_ptr;

	size_t hash_index = DEBUG_NEW_HASH(memory_ptr);
	ptr->next = new_ptr_list[hash_index];
#ifdef DEBUG_NEW_NO_FILENAME_COPY
	ptr->file = file;
#else
	strncpy(ptr->file, file, DEBUG_NEW_FILENAME_LEN - 1);
	ptr->file[DEBUG_NEW_FILENAME_LEN - 1] = '\0';
#endif
	ptr->line = line;
	ptr->size = size;
	ptr->caller_add = add;
	new_ptr_list[hash_index] = ptr;
	if (isPrint)
	{
		if(0 != line || true == isPrintUnknow)
		{
			printLog(outFile, "%p ===> new:  allocated  0x%p (size %u, %s:%d)\n",add, memory_ptr, size, file, line);
			fflush(outFile);
		}
	}
	thread_unlock();
	return memory_ptr;
}


void* operator new(size_t size, const char* file, int line, void* add)
{
	if (get_isRecode())
	{
		return record_new(size, file, line, add);
	}
	else
	{
		return malloc(size);
	}
}

void* operator new[](size_t size, const char* file, int line, void* add)
{
	return operator new(size, file, line, add);
}

void* operator new(size_t size)
{
	void* add = __builtin_return_address(0);
	return operator new(size, "<Unknown>", 0, add);
}

void* operator new[](size_t size)
{
	void* add = __builtin_return_address(0);
	return operator new(size, "<Unknown>", 0, add);
}

void* operator new(size_t size, const std::nothrow_t&) throw()
{
	void* add = __builtin_return_address(0);
	return operator new(size, "<Unknown>", 0, add);
}

void* operator new[](size_t size, const std::nothrow_t&) throw()
{
	void* add = __builtin_return_address(0);
	return operator new(size, "<Unknown>", 0, add);
}

void recode_delete(void* pointer)
{
	if (pointer == NULL)
		return;

	thread_lock();

	size_t hash_index = DEBUG_NEW_HASH(pointer);
	new_ptr_list_t* ptr = new_ptr_list[hash_index];
	new_ptr_list_t* ptr_last = NULL;
	while (ptr)
	{
		//if ((char*)ptr + sizeof(new_ptr_list_t) == pointer)
		if (ptr->memory == pointer)
		{
			if (isPrint)
			{
				if(0 != ptr->line || true == isPrintUnknow)
				{
					printLog(outFile, "%p ===> delete: freeing  0x%p (size %u, %s:%d)\n", ptr->caller_add, pointer, ptr->size, ptr->file, ptr->line);
					fflush(outFile);
				}
			}
			if (ptr_last == NULL)
				new_ptr_list[hash_index] = ptr->next;
			else
				ptr_last->next = ptr->next;
			free(ptr->memory);
			free(ptr);
			ptr = NULL;

			thread_unlock();

			return;
		}
		ptr_last = ptr;
		ptr = ptr->next;
	}
	/*
	printLog(outFile, "delete: !!!ERRER!!! invalid pointer 0x%p\n", pointer);

	thread_unlock();
	abort();
	*/
	free(pointer);
	thread_unlock();
}

void operator delete(void* pointer)
{
	if (pointer != NULL)
	{
		recode_delete(pointer);
	/*
		if (get_isRecode())
		{
			recode_delete(pointer);
		}
		else
		{
			free(pointer);
		}
	*/
	}
	
}

void operator delete[](void* pointer)
{
	operator delete(pointer);
}

// Some older compilers like Borland C++ Compiler 5.5.1 and Digital Mars
// Compiler 8.29 do not support placement delete operators.
// NO_PLACEMENT_DELETE needs to be defined when using such compilers.
// Also note that in that case memory leakage will occur if an exception
// is thrown in the initialization (constructor) of a dynamically
// created object.
#ifndef NO_PLACEMENT_DELETE
void operator delete(void* pointer, const char* file, int line)
{
	//if (new_verbose_flag)
 	//	printf("info: exception thrown on initializing object at %p (%s:%d)\n", pointer, file, line);
	operator delete(pointer);
}

void operator delete[](void* pointer, const char* file, int line)
{
	operator delete(pointer, file, line);
}

void operator delete(void* pointer, const std::nothrow_t&)
{
	operator delete(pointer, "<Unknown>", 0);
}

void operator delete[](void* pointer, const std::nothrow_t&)
{
	operator delete(pointer, std::nothrow);
}
#endif // NO_PLACEMENT_DELETE

// Proxy class to automatically call check_leaks if new_autocheck_flag is set
class new_check_t
{
public:
	new_check_t() {initial();}
	~new_check_t()
	{
		if (new_autocheck_flag)
		{
			// Check for leakage.
			// If any leaks are found, set new_verbose_flag so that any
			// delete operations in the destruction of global/static
			// objects will display information to compensate for
			// possible false leakage reports.

			//if (check_leaks())
			//	new_verbose_flag = true;
		}
	}
};
static new_check_t new_check_object;

void ShowMemoryLeak()
{
	if (get_isRecode())
	{
		check_leaks();
	}
	else
	{
		printLog(logFile, "Memory Record is not opened!\n");
	}
}

int command_parser(char* str)
{
	printf("read msg:%s ; compare = %d\n", str, strcmp(str, "print_on"));
	if(0 == strcmp(str, "start_record"))
	{
		start_record();
		return 1;
	}
	if(0 == strcmp(str, "stop_record"))
	{
		stop_record();
		return 1;
	}
	if(0 == strcmp(str, "memory_check"))
	{
		ShowMemoryLeak();
		//check_leaks();
		return 1;
	}
	if(0 == strcmp(str, "print_on"))
	{
		isPrint = true;
		return 1;
	}
	if(0 == strcmp(str, "print_off"))
	{
		isPrint = false;
		return 1;
	}
	if(0 == strcmp(str, "print_unknow_off"))
	{
		isPrintUnknow= false;
		return 1;
	}
	if(0 == strcmp(str, "print_unkonw_on"))
	{
		isPrintUnknow = true;
		return 1;
	}
	printf("Nothing has bean done!\n");
	return 0;
}

void file_dealer(char* filename)
{
#ifdef WIN32
	FILE* file = fopen(filename, "r");
	printf("openning file:%s\n" , filename);
	if(NULL == file)
	{
		printf("%s cannot open\n" , filename);
		return;
	}
	char mystring [256];
	while(NULL != fgets (mystring , 256 , file)){
		if(strlen(mystring) > 0)
			mystring[strlen(mystring)-1] = 0;
		command_parser(mystring);
	}
	fclose(file);
#else
	char filepath[256] = {0};
	sprintf(filepath, "/tmp/memory_leak_check/%s", filename);
	FILE* file = fopen(filepath, "r");
	printf("openning file:%s\n" , filepath);
	if(NULL == file)
	{
		printf("%s cannot open\n" , filepath);
		return;
	}
	char mystring [256];
	while(NULL != fgets (mystring , 256 , file)){
		if(strlen(mystring) > 0)
			mystring[strlen(mystring)-1] = 0;
		command_parser(mystring);
	}
	fclose(file);
#endif
}

#define EVENT_SIZE ( sizeof (struct inotify_event) )+
#define BUF_LEN ( 1024 * ( EVENT_SIZE 16 ) )
extern "C" int read(int, char*, size_t);
extern "C" void close(int);
void file_monitor()
{
#ifdef WIN32
	FILE* infile = fopen("C:\\memory_ckeck", "r");
	if (NULL == infile)
	{
		infile = fopen("C:\\memory_ckeck", "w");
	}
	fclose(infile);
	while(1)
	{
		 file_dealer("C:\\memory_ckeck");
		 Sleep(1000);
	}

#else
	int length, i = 0;
	int fd;
	int wd;
	char buffer[BUF_LEN];

	system( "mkdir -p /tmp/memory_leak_check");
	system( "touch /tmp/memory_leak_check/memory_check_cmd");
    
    	fd = inotify_init();
    
    if ( fd < 0 ) {
		return;
    }
  
    wd = inotify_add_watch( fd, "/tmp/memory_leak_check",  IN_MODIFY | IN_CREATE | IN_DELETE );

	while(length = read( fd, buffer, BUF_LEN )){
		if ( length < 0 ) {
       		return;
    		}
  
	    while ( i < length ) {
	       struct inotify_event *event = ( struct inotify_event * ) &buffer[ i ];
	       
	       if ( event->len ) {
	         if ( event->mask & IN_CREATE ) {
	             if ( event->mask & IN_ISDIR ) {
	                //printf( "The directory %s was created.\n", event->name );
	             }
	             else {
	                //printf( "The file %s was created.\n", event->name );
	             }
	       }
	         else if ( event->mask & IN_DELETE ) {
	             if ( event->mask & IN_ISDIR ) {
	                //printf( "The directory %s was deleted.\n", event->name );
	             }
	             else {
	                //printf( "The file %s was deleted.\n", event->name );
	             }
	        }
	        else if ( event->mask & IN_MODIFY ) {
	            if ( event->mask & IN_ISDIR ) {
	                //printf( "The directory %s was modified.\n", event->name );
	            }
	            else {
				printf( "The file %s was modified.\n", event->name );
				if( 0 == strcmp(event->name, "memory_check_cmd")){
					file_dealer(event->name);
				}
	           }
	        }
	  
	     }
	  
	      i = EVENT_SIZE event->len;
	  
	    }
  		i = 0;
	}
  
    ( void ) inotify_rm_watch( fd, wd );
    ( void ) close( fd );
#endif
}

static int isStarted = 0;
#define SERVER_PORT 12346
#define MAXBUF 1024
void server_monitor()
{
	char buf[MAXBUF + 1];
	int sockfd = 0;
	int new_fd = 0;
	struct sockaddr_in my_addr;
	struct sockaddr_in their_addr;

	if ((sockfd = socket(PF_INET, SOCK_STREAM, 0)) == -1)
	{
		perror("socket error!\n");
		//exit(1);
        return;
	}
	else printf("socket created!\n");
    isStarted = 1;

	bzero(&my_addr, sizeof(my_addr));
	my_addr.sin_family = PF_INET;
	my_addr.sin_port = htons(SERVER_PORT);
	my_addr.sin_addr.s_addr = INADDR_ANY;

	if (bind(sockfd, (struct sockaddr *) &my_addr, sizeof(struct sockaddr)) == -1)
	{
		perror("bind error!\n");
		//exit(1);
        return;
	}
	else printf("binded\n");

	if (listen(sockfd, 1) == -1) 
	{
		perror("listen error!\n");
		//exit(1);
        return;
	}
	else printf("begin listen prot:%d\n", SERVER_PORT);

	unsigned int len = 0;
	while(1) {
		len = sizeof(struct sockaddr);
		if ((new_fd = accept(sockfd, (struct sockaddr *)&their_addr, &len)) == -1)
		{
			perror("accept error!\n");
			//exit(errno);
            return;
		}

		bzero(buf, MAXBUF + 1);
		/* 接收客户端的消息 */
		len = recv(new_fd, buf, MAXBUF, 0);
		if(len > 0)
		{
			printf("successfully receive message'%s', Length:%d\n", buf, len);
			if(command_parser(buf))
			{
				len = send(new_fd, "OK", strlen("OK"), 0);
			}
			else
				len = send(new_fd, "Command not right", strlen("Command not right"), 0);
			
			if(len < 0) {
				printf("error send:%d, errorNo:'%s'\n", buf, errno, strerror(errno));
			}
			else printf("Successfully sent!");
		}
		close(new_fd);
		
	}

	close(sockfd);
}


#ifdef WIN32
unsigned int WINAPI thread_file_monitor(void * tid)
#else
void* thread_file_monitor(void* parm)
#endif
{
    printf("Monitor Threat Started!.......\n");
	if(!isStarted)
    {
        server_monitor();
    }
//	while(1)
	//{
	//	file_monitor();
	//}
    printf("Monitor Threat EXIT!.......\n");
#ifdef WIN32
	return 0;
#endif
}




