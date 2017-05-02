JFLAGS = -g
JC = javac

default: all
	
all:
	${JC} ${JFLAGS} masterbot.java commandLine.java commandSender.java slave.java commandParser.java msgReceiver.java slavebot.java portscanner.java ipscanner.java

clean:
	$(RM) *.class
