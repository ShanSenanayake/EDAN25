import java.util.Iterator;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.BitSet;
import java.lang.Math;


class Monitor{
	private int nthread;
	private int waitingThreads;
	private boolean finished;
	private LinkedList<Vertex> list;
	private int capacity;


	public Monitor(int nthread, int capacity){
		this.capacity = capacity;
		this.nthread = nthread;
		waitingThreads = 0;
		finished = false;
		list = new LinkedList<Vertex>();
	}

	public synchronized  LinkedList<Vertex> waitForNewWorklist()throws InterruptedException {
		waitingThreads++;
		if (waitingThreads >= nthread){
			finished = true;
			notifyAll();
			throw new InterruptedException();
		}
		while(list.isEmpty()){
			wait();
			if (finished){
				throw new InterruptedException();
			}
		}
		LinkedList<Vertex> new_list = new LinkedList<Vertex>();
		int size =Math.min(capacity, (int)(Math.ceil(list.size() / waitingThreads)));
		if(size <= 0){
			size = 1;
		}
		for (int i = 0; i<size; i++){
			new_list.add(list.poll());
		}

		waitingThreads--;
		return new_list;
	}

	public synchronized LinkedList<Vertex> addToNetworkList(LinkedList<Vertex> worklist) {
		int size = worklist.size() / nthread;
		LinkedList<Vertex> new_list = new LinkedList<Vertex>();
		for (int i = 0; i<size;i++){
			new_list.add(worklist.poll());
		}
		list.addAll(worklist);
		notifyAll();
		return new_list;
	}


}


class Worker extends Thread{
	private int count;
	private int capacity;
	private int id;
	private LinkedList<Vertex> worklist;	
	private Monitor monitor;

	public Worker(Monitor monitor ,LinkedList<Vertex> worklist, int id, int capacity){
		this.monitor = monitor;
		this.worklist = worklist;
		this.capacity = capacity;
		this.id = id;
		count = 0;
	}

	public void run(){
		try{
			while(!isInterrupted()){
				if(worklist.size() >= capacity){
					worklist = monitor.addToNetworkList(worklist);
				}
				 if(worklist.isEmpty()){
					worklist = monitor.waitForNewWorklist();
				}
				Vertex v = worklist.poll();
				count++;
				v.listed = false;
				v.computeIn(worklist);
			}
		}catch(InterruptedException e){
		}
		System.out.println(id + ": " + count);

	}


}


class Random {
	int	w;
	int	z;

	public Random(int seed)
	{
		w = seed + 1;
		z = seed * seed + seed + 2;
	}

	int nextInt()
	{
		z = 36969 * (z & 65535) + (z >> 16);
		w = 18000 * (w & 65535) + (w >> 16);

		return (z << 16) + w;
	}
}

class Vertex {
	int			index;
	boolean			listed;
	LinkedList<Vertex>	pred;
	LinkedList<Vertex>	succ;
	BitSet			in;
	BitSet			out;
	BitSet			use;
	BitSet			def;

	Vertex(int i)
	{
		index	= i;
		pred	= new LinkedList<Vertex>();
		succ	= new LinkedList<Vertex>();
		in	= new BitSet();
		out	= new BitSet();
		use	= new BitSet();
		def	= new BitSet();
	}

	void computeIn(LinkedList<Vertex> worklist)
	{
		int			i;
		BitSet			old;
		Vertex			v;
		ListIterator<Vertex>	iter;

		iter = succ.listIterator();

		while (iter.hasNext()) {
			v = iter.next();
			out.or(v.in);
		}

		old = in;

		// in = use U (out - def)

		BitSet new_in = new BitSet();
		new_in.or(out);	
		new_in.andNot(def);	
		new_in.or(use);
		in = new_in;

		if (!in.equals(old)) {
			iter = pred.listIterator();

			while (iter.hasNext()) {
				v = iter.next();
				if (!v.listed) {
					worklist.addLast(v);
					v.listed = true;
				}
			}
		}
	}

	public void print()
	{
		int	i;

		System.out.print("use[" + index + "] = { ");
		for (i = 0; i < use.size(); ++i)
			if (use.get(i))
				System.out.print("" + i + " ");
		System.out.println("}");
		System.out.print("def[" + index + "] = { ");
		for (i = 0; i < def.size(); ++i)
			if (def.get(i))
				System.out.print("" + i + " ");
		System.out.println("}\n");

		System.out.print("in[" + index + "] = { ");
		for (i = 0; i < in.size(); ++i)
			if (in.get(i))
				System.out.print("" + i + " ");
		System.out.println("}");

		System.out.print("out[" + index + "] = { ");
		for (i = 0; i < out.size(); ++i)
			if (out.get(i))
				System.out.print("" + i + " ");
		System.out.println("}\n");
	}

}

class DataflowParallel3 {

	public static void connect(Vertex pred, Vertex succ)
	{
		pred.succ.addLast(succ);
		succ.pred.addLast(pred);
	}

	public static void generateCFG(Vertex vertex[], int maxsucc, Random r)
	{
		int	i;
		int	j;
		int	k;
		int	s;	// number of successors of a vertex.

		System.out.println("generating CFG...");

		connect(vertex[0], vertex[1]);
		connect(vertex[0], vertex[2]);

		for (i = 2; i < vertex.length; ++i) {
			s = (r.nextInt() % maxsucc) + 1;
			for (j = 0; j < s; ++j) {
				k = Math.abs(r.nextInt()) % vertex.length;
				connect(vertex[i], vertex[k]);
			}
		}
	}

	public static void generateUseDef(	
			Vertex	vertex[],
			int	nsym,
			int	nactive,
			Random	r)
	{
		int	i;
		int	j;
		int	sym;

		System.out.println("generating usedefs...");

		for (i = 0; i < vertex.length; ++i) {
			for (j = 0; j < nactive; ++j) {
				sym = Math.abs(r.nextInt()) % nsym;

				if (j % 4 != 0) {
					if (!vertex[i].def.get(sym))
						vertex[i].use.set(sym);
				} else {
					if (!vertex[i].use.get(sym))
						vertex[i].def.set(sym);
				}
			}
		}
	}

	public static void liveness(Vertex vertex[], int nthread, int nvertex)
	{
		Vertex			u;
		Vertex			v;
		int			i;
		LinkedList<Vertex>	worklist;
		long			begin;
		long			end;
		Worker			workers[] = new Worker[nthread];
		LinkedList<Vertex>	worklists[] = new LinkedList[nthread];
		int 			capacity = nvertex / nthread;
		
		System.out.println(capacity);
		System.out.println("computing liveness...");

		begin = System.nanoTime();
		Monitor monitor = new Monitor(nthread,capacity);
		worklist = new LinkedList<Vertex>();

		for (i = 0; i<nthread; ++i){
			worklists[i] = new LinkedList<Vertex>();
		}

		for (i = 0; i<vertex.length;++i){
			worklists[i % nthread].addLast(vertex[i]);
			vertex[i].listed = true;
		}

		for (i = 0; i<nthread; ++i){
			workers[i] = new Worker(monitor,worklists[i], i, capacity);
			workers[i].start();
		}

		for (i = 0; i<nthread; ++i){
			try{
				workers[i].join();
			}catch(Exception e){
			}
		}
		end = System.nanoTime();
		System.out.println("T = " + (end-begin)/1e9 + " s");
	}

	public static void main(String[] args)
	{
		int	i;
		int	nsym;
		int	nvertex;
		int	maxsucc;
		int	nactive;
		int	nthread;
		boolean	print;
		Vertex	vertex[];
		Random	r;

		r = new Random(1);

		nsym = Integer.parseInt(args[0]);
		nvertex = Integer.parseInt(args[1]);
		maxsucc = Integer.parseInt(args[2]);
		nactive = Integer.parseInt(args[3]);
		nthread = Integer.parseInt(args[4]);
		print = Integer.parseInt(args[5]) != 0;

		System.out.println("nsym = " + nsym);
		System.out.println("nvertex = " + nvertex);
		System.out.println("maxsucc = " + maxsucc);
		System.out.println("nactive = " + nactive);

		vertex = new Vertex[nvertex];

		for (i = 0; i < vertex.length; ++i)
			vertex[i] = new Vertex(i);

		generateCFG(vertex, maxsucc, r);
		generateUseDef(vertex, nsym, nactive, r);
		liveness(vertex,nthread, nvertex);

		if (print)
			for (i = 0; i < vertex.length; ++i)
				vertex[i].print();
	}
}
