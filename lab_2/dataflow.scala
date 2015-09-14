import scala.actors._
import scala.collection.immutable.HashMap;
import java.util.BitSet;

// LAB 2: some case classes but you need additional ones too.

case class Start();
case class Stop();
case class Done();
case class Ready();
case class Go();
case class Change(newIn: BitSet, i: Int);
case class UpdateIn(newIn: BitSet, newOut: BitSet);
case class SendMeIn();
case class Compute(inMap: Map[Int, BitSet], out: BitSet, defs: BitSet, uses: BitSet);


class Random(seed: Int) {
        var w = seed + 1;
        var z = seed * seed + seed + 2;

        def nextInt() =
        {
                z = 36969 * (z & 65535) + (z >> 16);
                w = 18000 * (w & 65535) + (w >> 16);

                (z << 16) + w;
        }
}

class Controller(val cfg: Array[Vertex]) extends Actor {
  var started = 0;
  val begin   = System.currentTimeMillis();

  // LAB 2: The controller must figure out when
  //        to terminate all actors somehow.

  def act() {
    react {
      case Ready() => {
        started += 1;
        //println("controller has seen " + started);
        if (started == cfg.length) {
          for (u <- cfg)
            u ! new Go;
        }
        act();
      }
      case Done() => {
        started -= 1;
        println("CDONE " + started);
        if(started == 0){
          cfg.map(x => x.print);
          cfg.map(x =>x ! new Stop);
        }else{
          act();
        }
      }
    }
  }
}

class ComputeVertex() extends Actor {

    def act(){
      react {
        case Compute(inMap: Map[Int, BitSet], out: BitSet, defs: BitSet, uses: BitSet) => {
          println("computing");
          inMap.values.map(x => out.or(x));
          var in: BitSet = new BitSet();
          // from java-----------

          // in = use U (out - def)

          in.or(out);
          in.andNot(defs);
          in.or(uses);
          //----------------------
          sender ! new UpdateIn(in, out);
          act();
        }

        case Stop() => {
        }

      }
    }
}

class Vertex(val index: Int, s: Int, val controller: Controller) extends Actor {
  var pred: List[Vertex] = List();
  var succ: List[Vertex] = List();
  var inMap: Map[Int, BitSet] = new HashMap[Int, BitSet];
  var computer: ComputeVertex = new ComputeVertex();
  val uses               = new BitSet(s);
  val defs               = new BitSet(s);
  var in                 = new BitSet(s);
  var out                = new BitSet(s);

  def connect(that: Vertex)
  {
    //println(this.index + "->" + that.index);
    this.succ = that :: this.succ;
    that.pred = this :: that.pred;
  }

  def act() {
    react {
      case Start() => {
        controller ! new Ready;
        computer.start;
        println("started " + index);
        act();
      }

      case Go() => {
        // LAB 2: Start working with this vertex.
        if(succ.size == 0){
          computer ! new Compute(inMap, out, defs, uses);
          controller ! new Done;
        }
        succ.map(x => x ! new SendMeIn);
        println("GO " + index + " succsize " + succ.size);
        succ.map(x => println("giving sendmein to " + x.index +" form " + index ));
        act();
      }

      case Change(newIn: BitSet, i: Int) => {
        inMap += (i -> newIn);
        println("CHANGE " + index + " mapsize " + inMap.size + " succsize " + succ.size);
        if (inMap.size == succ.size){
          computer ! new Compute(inMap,out,defs,uses);

        }

        act();
      }

      case UpdateIn(newIn: BitSet, newOut: BitSet) => {
        out = newOut;
        println("UPDATEIN " + index);
        if(!in.equals(newIn)){
          in = newIn;
          println("send change " + index);
          pred.map(x => println("sending change form " + index + " to " + x.index));
          pred.map(x => x ! new Change(in, index));
        }else{
          println("should be done " + index);
          controller ! new Done;
        }

        act();
      }

      case SendMeIn() => {
        println("sendmein " + index);
        sender ! new Change(in, index);
        act();
      }
      case Stop()  => {
        println("stop " + index);
        computer ! new Stop();
      }
    }
  }

  def printSet(name: String, index: Int, set: BitSet) {
    System.out.print(name + "[" + index + "] = { ");
    for (i <- 0 until set.size)
      if (set.get(i))
        System.out.print("" + i + " ");
    println("}");
  }

  def print = {
    printSet("use", index, uses);
    printSet("def", index, defs);
    printSet("in", index, in);
    println("");
  }
}

object Driver {
  val rand    = new Random(1);
  var nactive = 0;
  var nsym    = 0;
  var nvertex = 0;
  var maxsucc = 0;

  def makeCFG(cfg: Array[Vertex]) {

    cfg(0).connect(cfg(1));
    cfg(0).connect(cfg(2));

    for (i <- 2 until cfg.length) {
      val p = cfg(i);
      val s = (rand.nextInt() % maxsucc) + 1;

      for (j <- 0 until s) {
        val k = cfg((rand.nextInt() % cfg.length).abs);
        p.connect(k);
      }
    }
  }

  def makeUseDef(cfg: Array[Vertex]) {
    for (i <- 0 until cfg.length) {
      for (j <- 0 until nactive) {
        val s = (rand.nextInt() % nsym).abs;
        if (j % 4 != 0) {
          if (!cfg(i).defs.get(s))
            cfg(i).uses.set(s);
        } else {
          if (!cfg(i).uses.get(s))
            cfg(i).defs.set(s);
        }
      }
    }
  }

  def main(args: Array[String]) {
    nsym           = args(0).toInt;
    nvertex        = args(1).toInt;
    maxsucc        = args(2).toInt;
    nactive        = args(3).toInt;
    val print      = args(4).toInt;
    val cfg        = new Array[Vertex](nvertex);
    val controller = new Controller(cfg);

    controller.start;

    println("generating CFG...");
    for (i <- 0 until nvertex)
      cfg(i) = new Vertex(i, nsym, controller);

    makeCFG(cfg);
    println("generating usedefs...");
    makeUseDef(cfg);

    println("starting " + nvertex + " actors...");

    for (i <- 0 until nvertex)
      cfg(i).start;

    for (i <- 0 until nvertex)
      cfg(i) ! new Start;

  /*  if (print != 0)
      for (i <- 0 until nvertex)
        cfg(i).print;*/
  }
}
