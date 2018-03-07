import java.util.*;
import java.util.stream.*;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.sat4j.specs.TimeoutException;

import com.google.common.collect.ImmutableList;
import automata.sfa.*;
import theory.intervals.*;


/**
 * Policy: 0 iniziale e finale
 * 0 --> 0 : =10
   0 --> 1: >10
   1 --> 1: =10
   1 --> 0: <10


   Automa A: 0 iniziale e finale
   0 --> 0: =0
   0 --> 1: even
   1 --> 1: =0
   1 --> 0: odd

 * 
 * Per rendere le cose semplici per ora uso una teoria di intervalli 
 * dove odd e even indicano i pari e i dispari nell'intervallo [0,20].
 * Per gli interi generali (o per esprimere predicati in modo diverso che come insieme di intervalli) 
 * credo che bisogna implementare una teoria a parte.
 */
public class Example1 {

	static IntPred equalZero = new IntPred(0);
	static IntPred equalTen = new IntPred(10);
	static IntPred lessTen = new IntPred(0,9);
	static IntPred greaterTen = new IntPred(11,20);
	static IntPred odd = ofArray(1,3,5,7,9,11,13,15,17,19);
	static IntPred evenP = ofArray(2,4,6,8,10,12,14,16,18,20);
	
	static IntegerSolver ba = new IntegerSolver();
	static SFA<IntPred,Integer> policy = makePolicy();
	static SFA<IntPred,Integer> automataA = makeAutomataA();
	static SFA<IntPred,Integer> productPA = makeIntersection();
	
	
	
	private static IntPred ofArray(Integer... ints) {
		return new IntPred(ImmutableList
				.copyOf(Arrays.stream(ints).map(n -> ImmutablePair.of(n, n)).collect(Collectors.toList())));
	}
	
	private static SFA<IntPred, Integer> makeIntersection() {
		try {
			return SFA.intersection(policy, automataA, ba, 5000);
		} catch (TimeoutException e) {
			System.err.println("Fail during the intersection");
			e.printStackTrace();
			return null;
		}
	}

	private static SFA<IntPred, Integer> makePolicy(){
		List<SFAMove<IntPred, Integer>> trans = Arrays.asList(
				new SFAInputMove<IntPred, Integer>(0,0,equalTen),
				new SFAInputMove<IntPred, Integer>(1,1,equalTen),
				new SFAInputMove<IntPred, Integer>(0,1,greaterTen),
				new SFAInputMove<IntPred, Integer>(1,0,lessTen)
				);
		try {
		return SFA.MkSFA(trans, 0, Arrays.asList(0), ba);
		}catch(TimeoutException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static SFA<IntPred, Integer> makeAutomataA(){
		List<SFAMove<IntPred, Integer>> trans = Arrays.asList(
				new SFAInputMove<IntPred, Integer>(0,0,equalZero),
				new SFAInputMove<IntPred, Integer>(1,1,equalZero),
				new SFAInputMove<IntPred, Integer>(0,1,evenP),
				new SFAInputMove<IntPred, Integer>(1,0,odd)
				);
		try {
		return SFA.MkSFA(trans, 0, Arrays.asList(0), ba);
		}catch(TimeoutException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void testPrint() {
		System.out.println("This is the policy automata\n" + policy.toString());
		System.out.println("This is the automa A\n" + automataA.toString());
		System.out.println("The intersection automata is\n" + productPA.toString());
	}
	
	public void testAccept() {
		List<Integer> inputPolicy1 = Arrays.asList(10,10,10,11,10,9,10,10);
		List<Integer> inputAutomata1 = Arrays.asList(0,2,0,0,1,0);
		List<Integer> inputProduct1 = Arrays.asList(10,0,3,10);
		try {
			System.out.println("Policy accepts: " + inputPolicy1);
			assert(policy.accepts(inputPolicy1, ba));
			List<Integer> inputPolicy2 = inputPolicy1.subList(0, 4);
			System.out.println("Policy does not accept: " + inputPolicy2);
			assert(!policy.accepts(inputPolicy2, ba));
			
			System.out.println("AutomataA accepts: " + inputAutomata1);
			assert(automataA.accepts(inputAutomata1, ba));
			List<Integer> inputAutomataA2 = inputAutomata1.subList(0, 3);
			System.out.println("AutomataA does not accept: " + inputAutomataA2);
			assert(!automataA.accepts(inputAutomataA2, ba));
			
			System.out.println("Product accepts: " + inputProduct1);
			assert(productPA.accepts(inputAutomata1, ba));
			System.out.println("Product does not accept: " + inputAutomata1);
			assert(!productPA.accepts(inputAutomata1, ba));
			
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	}
}

