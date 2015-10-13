CC		= gcc

CFLAGS		= -Wall -pedantic -std=c99 -m32 -g
CFLAGS		= -m64 -g -Wall -pedantic -Werror -std=c99
CFLAGS		= -O3 -maltivec -Wall -pedantic -std=c99
CFLAGS		=  -Wall -pedantic -std=c99

OBJS		= main.o list.o error.o random.o set.o dataflow.o

OUT		= live

LDFLAGS		= -lpthread

S=50000
V=50000
U=4
A=1000
T=4
P=0
SEED=1

all: $(OBJS)
	$(CC) $(CFLAGS) $(OBJS) $(LDFLAGS) -o $(OUT)
	#valgrind --tool=helgrind ./$(OUT) $(S) $(V) $(U) $(A) $(T) $(P) $(L)
	./$(OUT) $(S) $(V) $(U) $(A) $(T) $(P) $(SEED)

clean:
	rm -f $(OUT) $(OBJS) cfg.dot
