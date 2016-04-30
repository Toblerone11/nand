###############################################################################
#
# Makefile for Java project
#
# Students:
# Ron Pick, 		ID 301643334, ron.pick@mail.huji.ac.il
# Ron Likvornik, 	ID 302574694 ron.likvornik@mail.huji.ac.il
###############################################################################

JAVAC=javac
JAVACFLAGS=

SRCS=*.java
EXEC=Assembler

TAR=tar
TARFLAGS=cvf
TARNAME=project6.tar
TARSRCS=$(SRCS) $(EXEC) README Makefile

all: compile

compile:
	$(JAVAC) $(JAVACFLAGS) $(SRCS)
	chmod +x $(EXEC)

tar:
	$(TAR) $(TARFLAGS) $(TARNAME) $(TARSRCS)

clean:
	rm -f -r *.class *~

