#include <assert.h>
#include <limits.h>
#include <pthread.h>
#include <stddef.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/times.h>
#include <sys/time.h>
#include <unistd.h>

#define NBR_THREADS (4)


pthread_mutex_t thread_sum;

static int limit = 4;

int n_thread = 1;

struct arg_struct_t
{
	void* base;
	size_t n;
	size_t s;
	int i;
	int (*cmp)(const void*, const void*);
};

typedef  struct arg_struct_t arg_struct_t;

void par_sort(
	void*		base,	// Array to sort.
	size_t		n,	// Number of elements in base.
	size_t		s,	// Size of each element.
	int 		i,
	int		(*cmp)(const void*, const void*)); // Behaves like strcmp

static double sec(void)
{
	struct timeval t;
	gettimeofday(&t, NULL);
	return t.tv_sec+(t.tv_usec/1000000.0);
}

void thread_sort(void *args)
{
	arg_struct_t* t = (arg_struct_t*)args;
	par_sort(t->base,t->n,t->s,t->i,t->cmp);
}

void par_sort(
	void*		base,	// Array to sort.
	size_t		n,	// Number of elements in base.
	size_t		s,	// Size of each element.
	int 		i,
	int		(*cmp)(const void*, const void*)) // Behaves like strcmp
{

	//lock and handle mutex stuff
	pthread_mutex_lock(&thread_sum);

	//do stuff with mutex
	//unlock mutex stuff
	pthread_mutex_unlock(&thread_sum);

	pthread_t t1,t2;
	if(i+2<NBR_THREADS)
	{
		arg_struct_t arg1 = {base, n/2, s, i+1, cmp};
		pthread_create(&t1,NULL,thread_sort,&arg1);
		arg_struct_t arg2 = {(char*)(base)+(s*(n/2)), n/2, s, i+2,cmp};
		pthread_create(&t2,NULL,thread_sort,&arg2);
		pthread_join(t1,NULL);
		pthread_join(t2,NULL);
//fixa ihop delarna
	}else	if(i+1<NBR_THREADS)
	{
		arg_struct_t arg1 = {base, n/2, s, i+1, cmp};
		pthread_create(&t1,NULL,thread_sort,&arg1);
		pthread_join(t1,NULL);
		qsort((char*)(base)+(s*(n/2)), n/2, s,cmp);
		//fixa ihop delarna
	} else{
		qsort(base,n,s,cmp);
	}


}

static int cmp(const void* ap, const void* bp)
{
	/* you need to modify this function to compare doubles. */

	const double* apd = ap;
	const double* bpd = bp;

	return *apd-*bpd;
}

int main(int ac, char** av)
{
	int		n = 2000000;
	int		i;
	double*		a;
	double		start, end;

	pthread_mutex_init(&thread_sum, NULL);

	if (ac > 1)
		sscanf(av[1], "%d", &n);

	srand(getpid());


	a = malloc(n * sizeof a[0]);
	for (i = 0; i < n; i++)
		a[i] = rand();

	start = sec();

#ifdef PARALLELL
	par_sort(a, n, sizeof a[0],0, cmp);
#else
	qsort(a, n, sizeof a[0], cmp);
#endif

	end = sec();

	printf("%1.2f s\n", end - start);

	free(a);

	return 0;
}
