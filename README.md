# spring-boot-james-smtp-server

Simple SMTP Server using James and Spring Boot which just logs sender, recipient and size for every message. 
It is very easy to implement your use an existing Apache James Hook or implement your own Hook. 

## Run

```bash
mvn spring-boot:run
```

## Test

```bash
telnet localhost 10025
Trying ::1...
Connected to localhost.
Escape character is '^]'.
220 LT02064.local SMTP Server (JAMES SMTP Protocols Server) ready
ehlo a
250-LT02064.local Hello a [0:0:0:0:0:0:0:1])
250-PIPELINING
250-ENHANCEDSTATUSCODES
250 8BITMIME
mail from:<hello@world.com>
250 2.1.0 Sender <hello@world.com> OK
rcpt to:<hello@world.com>
250 2.1.5 Recipient <hello@world.com> OK
data
354 Ok Send data ending with <CRLF>.<CRLF>
Subject: hello

world

.
250 Command accepted
quit
221 2.0.0 LT02064.local Service closing transmission channel
Connection closed by foreign host.

```