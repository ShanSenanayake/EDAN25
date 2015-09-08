import java.util.Iterator;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.BitSet;



class Worklist<T>{
	private LinkedList<Vertex> list;
	private int waitingThreads; 
	private int kthreads;
	private int bagSize;
	private boolean finished;


	public Worklist(LinkedList<Vertex> list ,int kthreads){
		this.list = list;
		waitingThreads = 0;
		bagSize = 4;
		this.kthreads = kthreads;
		finished = false;
	}

	public synchronized Vertex getNode() throws InterruptedException{
		waitingThreads++;
		if(waitingThreads >= kthreads && list.isEmpty()){
			finished = true;
			notifyAll();
			return null;
		}

		while(list.isEmpty()){
			wait();
			if(finished){
				return null;
			}
		}

		waitingThreads--;
		return list.poll();
	}

	public synchronized void addNode(Vertex v){
		list.addLast(v);
		notifyAll();
	}

	public synchronized LinkedList<Vertex> getAndAddNode(LinkedList<Vertex> addList) throws InterruptedException{
		addNodes(addList);
		waitingThreads++;
		if(waitingThreads >= kthreads && list.isEmpty()){
			finished = true;
			notifyAll();
			return null;
		}
		while(list.isEmpty()){
			wait();
			if(finished){
				return null;
			}

		}
		waitingThreads--;
		return bag();
	}

	private void addNodes(LinkedList<Vertex> addList){
		if(addList != null && !addList.isEmpty()){
			list.addAll(addList);
			notifyAll();
		}
	}

	private LinkedList<Vertex> bag(){
		LinkedList<Vertex> worklist = new LinkedList<Vertex>();
		for(int i = 0; i<bagSize; i++){
			Vertex v = list.poll();
			if (v == null){
				return worklist;
			}
			worklist.add(v);
		}
		return worklist;
	}



}
class Worker extends Thread{
	private Worklist list;
	private int count;


	public Worker(Worklist list){
		this.list = list;
		count = 0;
	}

	public void run(){
		LinkedList<Vertex> worklist = new LinkedList<Vertex>();
		try{
			while(!isInterrupted()){

				LinkedList<Vertex> jobs = list.getAndAddNode(worklist);
				//Vertex jobs = list.getNode();
				worklist = new LinkedList<Vertex>();
				if(jobs == null){
					throw new InterruptedException();
				}
				/*jobs.listed = false;
				count++;
				jobs.computeIn(list);*/
				for (Vertex v: jobs){
					count++;
					v.listed = false;
					v.computeIn(worklist);

				}
			}
		}catch(InterruptedException e){
		}
		System.out.println(count);
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

class DataflowParallel2 {

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

	public static void liveness(Vertex vertex[], int kthreads)
	{
		Vertex			u;
		Vertex			v;
		int			i;
		LinkedList<Vertex>	worklist;
		long			begin;
		long			end;

		System.out.println("computing liveness...");

		begin = System.nanoTime();
		worklist = new LinkedList<Vertex>();

		for (i = 0; i < vertex.length; ++i) {
			worklist.addLast(vertex[i]);
			vertex[i].listed = true;
		}

		Worklist list = new Worklist(worklist, kthreads);
		Worker threads[] = new Worker[kthreads];
		for (i = 0; i<kthreads; i++){
			threads[i] = new Worker(list);
			threads[i].start();
		}
		for(i = 0; i<kthreads; i++){
			try{
				threads[i].join();
			}catch(InterruptedException e){

			}
		}
		/*while (!worklist.isEmpty()) {
			u = worklist.remove();
			u.listed = false;
			u.computeIn(worklist);
		}*/
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
		liveness(vertex,nthread);

		if (print)
			for (i = 0; i < vertex.length; ++i)
				vertex[i].print();
	}
}
