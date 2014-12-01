#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <sys/socket.h>
#include <resolv.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>

#define MAXBUF 1024
#define SERVER_PROT 12346
int main(int argc, char **argv)
{
    if(argc != 3)
    {
        printf("USAGE: <ip_address> <command>\n");
	    return 0;
    }
    int sockfd;
	struct sockaddr_in dest;
	char buffer[MAXBUF];
	char * cmd = 0;
	char * addr = 0;

	if (argc == 3) {
		cmd = argv[2];
		addr = argv[1];
	}
	if (argc == 2) {
		cmd = argv[1];
		addr = "127.0.0.1";
	}
	
	/* 创建一个 socket 用于 tcp 通信 */
	if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
		perror("Socket");
		exit(errno);
	}

	/* 初始化服务器端（对方）的地址和端口信息 */
	bzero(&dest, sizeof(dest));
	dest.sin_family = AF_INET;
	dest.sin_port = htons(SERVER_PROT);
	if (inet_aton(addr, (struct in_addr *) &dest.sin_addr.s_addr) == 0) {
		perror(addr);
		exit(errno);
	}

	/* 连接服务器 */
	if (connect(sockfd, (struct sockaddr *) &dest, sizeof(dest)) != 0) {
		perror("Connect error\n");
		exit(errno);
	}

	send(sockfd, cmd, strlen(cmd), 0);
	/* 接收对方发过来的消息，最多接收 MAXBUF 个字节 */
	bzero(buffer, MAXBUF);
	recv(sockfd, buffer, sizeof(buffer), 0);
	printf("%s\n", buffer);

	/* 关闭连接 */
	close(sockfd);
	return 0;
}
