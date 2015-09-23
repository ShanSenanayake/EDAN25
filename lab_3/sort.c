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
	int (*cmp)(const void*, const void*);
};

typedef  struct arg_struct_t arg_struct_t;

void par_sort(
	void*		base,	// Array to sort.
	size_t		n,	// Number of elements in base.
	size_t		s,	// Size of each eleme
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
	par_sort(t->base,t->n,t->s,t->cmp);
}

size_t partition(double* base,size_t size){
		double pivot = base[size/2];
		//printf("size %zu, pivot %f\n", size ,pivot);
		size_t i = 0;
		size_t j = size -1;
		while(i != j){

			//printf("%zu, %zu\n", i ,j);
			if(base[j] <= pivot){
				//printf("swapping\n");
				//swap
				double temp = base[j];
				base[j] = base[i];
				base[i] = temp;
				//printf("jm m1\n");
				i++;

			}else{
				j--;
			}
			/*if (base[i] > pivot && base[j] <= pivot){
					double temp = base[j];
					base[j] = base[i];
					base[i] = temp;
					i++;
					j--;
			}else if (base[i] <= pivot){

				i++;
			}else if(base[j] > pivot){
				j--;
			}*/


		}
		printf("pivot %1.2f\n", (double)(i)/size);
		if(base[i] > pivot)
			return i;
		else
			return i+1;

}

void par_sort(
	void*		base,	// Array to sort.
	size_t		n,	// Number of elements in base.
	size_t		s,	// Size of each element
	int		(*cmp)(const void*, const void*)) // Behaves like strcmp
{

	int split = 0;
	//lock and handle mutex stuff

	pthread_mutex_lock(&thread_sum);
	//change n in if to higher number to potentially  decrease load balancing problem
	if(n_thread<NBR_THREADS && n > 1 ){
		split = 1;
		n_thread++;
	}
	//do stuff with mutex
	//unlock mutex stuff
	pthread_mutex_unlock(&thread_sum);


	if(split){

		pthread_t t;
		size_t left_size = partition(base, n);
		size_t right_size  = n - left_size;
		void* new_base = ((double*)(base)) + ((left_size));
		arg_struct_t arg = {new_base,right_size,s,cmp};
		pthread_create(&t,NULL,thread_sort,&arg);
		par_sort(base,left_size,s,cmp);
		pthread_join(t,NULL);

	}

	/*pthread_t t1,t2;
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
	}*/ else{
		printf("workload %zu\n", n);
		qsort(base,n,s,cmp);

	}


}

static int cmp(const void* ap, const void* bp)
{
	/* you need to modify this function to compare doubles. */

	const double* apd = ap;
	const double* bpd = bp;

	//return *apd-*bpd;
	return *apd < *bpd ? -1 : *apd == *bpd ? 0 : 1;
}

int main(int ac, char** av)
{
	int		n = 2000000;
	int		i;
	double*		a;
	double*		b;
	double		start, end;

	pthread_mutex_init(&thread_sum, NULL);

	if (ac > 1)
		sscanf(av[1], "%d", &n);

	srand(getpid());


	a = malloc(n * sizeof a[0]);
	b = malloc(n* sizeof a[0]);
	for (i = 0; i < n; i++){
		a[i] = rand();
		b[i] = a[i];
	}

	start = sec();

	printf("runing parallell..\n");
	par_sort(a, n, sizeof a[0], cmp);


	end = sec();
	double par_time = end - start;
	start = sec();

printf("running qsort..\n");
	qsort(b,n,sizeof a[0], cmp);

	end = sec();

	printf("running assert...\n");
	for(i = 0; i<n;i++){
			assert(a[i] == b[i]);
	}

	printf("seq: %1.2f s\n", end - start);
	printf("par: %1.2f s\n",par_time);


	free(a);
	pthread_mutex_destroy(&thread_sum);
	pthread_exit(NULL);
	free(b);

	return 0;
}
