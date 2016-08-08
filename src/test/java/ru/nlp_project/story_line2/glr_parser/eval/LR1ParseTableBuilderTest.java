package ru.nlp_project.story_line2.glr_parser.eval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import ru.nlp_project.story_line2.glr_parser.TestFixtureBuilder;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol.SymbolTypes;

public class LR1ParseTableBuilderTest {

  private LR1ParseTableBuilder testable;
  private Grammar grammar;

  @Before
  public void setUp() throws Exception {
    grammar = new Grammar();
    testable = new LR1ParseTableBuilder(grammar);
  }

  /*
  1. S--- > CC
  2. C--- > cC
  3. C--- > d
  */
  @Test
  public void testCalculateGoto() {
    grammar.add(TestFixtureBuilder.createProjection("S", "CC"));
    grammar.add(TestFixtureBuilder.createProjection("C", "cC"));
    grammar.add(TestFixtureBuilder.createProjection("C", "d"));
    grammar.prepareGrammar();

    Set<LR1Point> closureSet = new HashSet<LR1Point>();
    // S -> *CC, $
    closureSet.add(new LR1Point(grammar, 0, 0, Symbol.EOI));
    // S -> *cC, c/d$
    closureSet.add(
        new LR1Point(grammar, 1, 0, new Symbol("c", SymbolTypes.Terminal)));
    closureSet.add(
        new LR1Point(grammar, 1, 0, new Symbol("d", SymbolTypes.Terminal)));
    // S -> *d, c/d$
    closureSet.add(
        new LR1Point(grammar, 2, 0, new Symbol("c", SymbolTypes.Terminal)));
    closureSet.add(
        new LR1Point(grammar, 2, 0, new Symbol("d", SymbolTypes.Terminal)));

    // GOTO(I,C)
    Set<LR1Point> goto1 = testable.calculateGoto(closureSet,
        new Symbol("C", SymbolTypes.NonTerminal));
    List<LR1Point> sortList = new ArrayList<LR1Point>(goto1);
    Collections.sort(sortList);
    assertEquals("[C->* d [EOI], C->* c C [EOI], S->C * C [EOI]]",
        sortList.toString());
    // GOTO(I,c)
    goto1 = testable.calculateGoto(closureSet,
        new Symbol("c", SymbolTypes.Terminal));
    sortList = new ArrayList<LR1Point>(goto1);
    Collections.sort(sortList);
    assertEquals("[C->* d [d], C->* d [c], C->c * C [d], C->c * C [c], C->* c C [d], C->* c C [c]]",
        sortList.toString());
    // GOTO(I,S)
    goto1 = testable.calculateGoto(closureSet,
        new Symbol("S", SymbolTypes.Terminal));
    sortList = new ArrayList<LR1Point>(goto1);
    Collections.sort(sortList);
    assertEquals("[]", sortList.toString());
  }

  /*
  1. S--- > CC
  2. C--- > cC
  3. C--- > d
   */
  @Test
  public void testCalculateClosure() {
    grammar.add(TestFixtureBuilder.createProjection("S", "CC"));
    grammar.add(TestFixtureBuilder.createProjection("C", "cC"));
    grammar.add(TestFixtureBuilder.createProjection("C", "d"));
    grammar.prepareGrammar();

    Set<LR1Point> closureSet = new HashSet<LR1Point>();
    // Типа инициализируем S' -> *S$
    closureSet.add(new LR1Point(grammar, 0, 0, Symbol.EOI));
    testable.calculateClosure(closureSet);

    List<LR1Point> sortList = new ArrayList<LR1Point>(closureSet);
    Collections.sort(sortList);
    assertEquals("[C->* d [d], C->* d [c], C->* c C [d], C->* c C [c], S->* C C [EOI]]",
        sortList.toString());
  }

  @Test
  public void testCalculateClosure_AnotherStep() {
    grammar.add(TestFixtureBuilder.createProjection("S", "CC"));
    grammar.add(TestFixtureBuilder.createProjection("C", "cC"));
    grammar.add(TestFixtureBuilder.createProjection("C", "d"));
    grammar.prepareGrammar();

    Set<LR1Point> closureSet = new HashSet<LR1Point>();
    closureSet.add(new LR1Point(grammar, 0, 1, Symbol.EOI));
    testable.calculateClosure(closureSet);

    List<LR1Point> sortList = new ArrayList<LR1Point>(closureSet);
    Collections.sort(sortList);
    assertEquals("[C->* d [EOI], C->* c C [EOI], S->C * C [EOI]]",
        sortList.toString());
  }

  @Test
  public void testCalculateClosureWithEpsilonSymbol() {
    grammar.add(TestFixtureBuilder.createProjection("S", "ABc"));
    grammar.add(TestFixtureBuilder.createProjection("A", "a"));
    grammar.add(TestFixtureBuilder.createProjection("B", "b"));
    grammar.add(TestFixtureBuilder.createProjection("B", "ε"));
    grammar.prepareGrammar();

    Set<LR1Point> closureSet = new HashSet<LR1Point>();
    closureSet.add(new LR1Point(grammar, 0, 0, Symbol.EOI));
    testable.calculateClosure(closureSet);

    List<LR1Point> sortList = new ArrayList<LR1Point>(closureSet);
    Collections.sort(sortList);
    assertEquals("[A->* a [c], A->* a [b], S->* A B c [EOI]]", sortList.toString());
  }

  @Test
  public void testCalculateClosureWithEpsilonSymbol2() {
    grammar.add(TestFixtureBuilder.createProjection("T", "S"));
    grammar.add(TestFixtureBuilder.createProjection("S", "aSA"));
    grammar.add(TestFixtureBuilder.createProjection("S", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("A", "ε"));
    grammar.prepareGrammar();

    Set<LR1Point> closureSet = new HashSet<LR1Point>();
    closureSet.add(new LR1Point(grammar, 0, 0, Symbol.EOI));
    testable.calculateClosure(closureSet);

    List<LR1Point> sortList = new ArrayList<LR1Point>(closureSet);
    Collections.sort(sortList);
    assertEquals("[S->EPSILON * [EOI], S->* a S A [EOI], T->* S [EOI]]",
        sortList.toString());
  }

  @Test
  public void testCalculateClosureWithEpsilonSymbol2_NextStep() {
    grammar.add(TestFixtureBuilder.createProjection("T", "S"));
    grammar.add(TestFixtureBuilder.createProjection("S", "aSA"));
    grammar.add(TestFixtureBuilder.createProjection("S", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("A", "ε"));
    grammar.prepareGrammar();

    Set<LR1Point> closureSet = new HashSet<LR1Point>();
    closureSet.add(new LR1Point(grammar, 1, 1, Symbol.EOI));
    testable.calculateClosure(closureSet);

    List<LR1Point> sortList = new ArrayList<LR1Point>(closureSet);
    Collections.sort(sortList);
    assertEquals("[S->EPSILON * [EOI], S->a * S A [EOI], S->* a S A [EOI]]",
        sortList.toString());
  }

  @Test
  public void testCalculateClosureWithEpsilonSymbol_NextStep() {
    grammar.add(TestFixtureBuilder.createProjection("S", "ABc"));
    grammar.add(TestFixtureBuilder.createProjection("A", "a"));
    grammar.add(TestFixtureBuilder.createProjection("B", "b"));
    grammar.add(TestFixtureBuilder.createProjection("B", "ε"));
    grammar.prepareGrammar();

    Set<LR1Point> closureSet = new HashSet<LR1Point>();
    closureSet.add(new LR1Point(grammar, 0, 1, Symbol.EOI));
    testable.calculateClosure(closureSet);

    List<LR1Point> sortList = new ArrayList<LR1Point>(closureSet);
    Collections.sort(sortList);
    assertEquals("[B->EPSILON * [c], B->* b [c], S->A * B c [EOI]]",
        sortList.toString());
  }

  @Test
  public void testFirst() {
    grammar.add(TestFixtureBuilder.createProjection("E", "TZ"));
    grammar.add(TestFixtureBuilder.createProjection("Z", "+TZ"));
    grammar.add(TestFixtureBuilder.createProjection("Z", ""));
    grammar.add(TestFixtureBuilder.createProjection("T", "FX"));
    grammar.add(TestFixtureBuilder.createProjection("X", "*FX"));
    grammar.add(TestFixtureBuilder.createProjection("X", ""));
    grammar.add(TestFixtureBuilder.createProjection("F", "(E)"));
    grammar.add(TestFixtureBuilder.createProjection("F", "i"));
    grammar.prepareGrammar("E");

    List<Symbol> args = new ArrayList<>();

    // FIRST (F) = FIRST(T) = FIRST(E) = {(,i}
    Collections.addAll(args, new Symbol("F", SymbolTypes.NonTerminal));
    Collection<Symbol> first = testable.calculateFirst(args);
    List<Symbol> sortList = new ArrayList<Symbol>(first);
    Collections.sort(sortList);
    assertEquals("[(, i]", sortList.toString());

    args.clear();
    Collections.addAll(args, new Symbol("T", SymbolTypes.NonTerminal));
    first = testable.calculateFirst(args);
    sortList = new ArrayList<Symbol>(first);
    Collections.sort(sortList);
    assertEquals("[(, i]", sortList.toString());

    args.clear();
    Collections.addAll(args, new Symbol("E", SymbolTypes.NonTerminal));
    first = testable.calculateFirst(args);
    sortList = new ArrayList<Symbol>(first);
    Collections.sort(sortList);
    assertEquals("[(, i]", sortList.toString());

    // FIRST (Z) = {+, epsilon}
    args.clear();
    Collections.addAll(args, new Symbol("Z", SymbolTypes.NonTerminal));
    first = testable.calculateFirst(args);
    sortList = new ArrayList<Symbol>(first);
    Collections.sort(sortList);
    assertEquals("[+]", sortList.toString());

    // FIRST (X) = {*, epsilon}
    args.clear();
    Collections.addAll(args, new Symbol("X", SymbolTypes.NonTerminal));
    first = testable.calculateFirst(args);
    sortList = new ArrayList<Symbol>(first);
    Collections.sort(sortList);
    assertEquals("[*]", sortList.toString());
  }

  @Test
  public void testFirstWithRecursiveEpsilonGrammar() {
    /*
    Z ->T;
    U->a  U;
    V->EPSILON;
    V->b V;
     */
    grammar.add(TestFixtureBuilder.createProjection("Z", "T"));
    grammar.add(TestFixtureBuilder.createProjection("U", "aU"));
    grammar.add(TestFixtureBuilder.createProjection("V", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("V", "Vb"));

    grammar.prepareGrammar();

    List<Symbol> args = new ArrayList<>();

    // FIRST(U) = {a}
    Collections.addAll(args, new Symbol("U", SymbolTypes.NonTerminal));
    Collection<Symbol> first = testable.calculateFirst(args);
    List<Symbol> sortList = new ArrayList<Symbol>(first);
    Collections.sort(sortList);
    assertEquals("[a]", sortList.toString());

    // FIRST(V) =1/ {b, EPSILON}
    args.clear();
    Collections.addAll(args, new Symbol("V", SymbolTypes.NonTerminal));
    first = testable.calculateFirst(args);
    sortList = new ArrayList<Symbol>(first);
    Collections.sort(sortList);
    assertEquals("[b, EPSILON]", sortList.toString());
  }

  @Test
  public void testFirstWithEpsilonSymbols() {
    /*
     * Example from
    Example from "Parsing Techniques: A Practical Guide by Dick Grune, Ceriel J. Jacobs"
      
    S ---> A | AB | B 
    A ---> C 
    B ---> D 
    C ---> p | ε
    D ---> q 
     */
    // ε
    grammar.add(TestFixtureBuilder.createProjection("S", "A"));
    grammar.add(TestFixtureBuilder.createProjection("S", "B"));
    grammar.add(TestFixtureBuilder.createProjection("S", "AB"));
    grammar.add(TestFixtureBuilder.createProjection("A", "C"));
    grammar.add(TestFixtureBuilder.createProjection("B", "D"));
    grammar.add(TestFixtureBuilder.createProjection("C", "p"));
    grammar.add(TestFixtureBuilder.createProjection("C", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("D", "q"));

    grammar.prepareGrammar();

    List<Symbol> args = new ArrayList<>();

    // FIRST(A) = {ε, p}
    Collections.addAll(args, new Symbol("A", SymbolTypes.NonTerminal));
    Collection<Symbol> first = testable.calculateFirst(args);
    List<Symbol> sortList = new ArrayList<Symbol>(first);
    Collections.sort(sortList);
    assertEquals("[p, EPSILON]", sortList.toString());

    // FIRST(AB) = {p, q}
    args.clear();
    Collections.addAll(args, new Symbol("A", SymbolTypes.NonTerminal));
    Collections.addAll(args, new Symbol("B", SymbolTypes.NonTerminal));
    first = testable.calculateFirst(args);
    sortList = new ArrayList<Symbol>(first);
    Collections.sort(sortList);
    assertEquals("[p, q]", sortList.toString());

    // FIRST(S) = {ε, p, q}
    args.clear();
    Collections.addAll(args, new Symbol("S", SymbolTypes.NonTerminal));
    first = testable.calculateFirst(args);
    sortList = new ArrayList<Symbol>(first);
    Collections.sort(sortList);
    assertEquals("[p, q, EPSILON]", sortList.toString());

    // FIRST(C) = { ε, p}
    args.clear();
    Collections.addAll(args, new Symbol("C", SymbolTypes.NonTerminal));
    first = testable.calculateFirst(args);
    sortList = new ArrayList<Symbol>(first);
    Collections.sort(sortList);
    assertEquals("[p, EPSILON]", sortList.toString());

    // FIRST(B) = {q}
    args.clear();
    Collections.addAll(args, new Symbol("B", SymbolTypes.NonTerminal));
    first = testable.calculateFirst(args);
    sortList = new ArrayList<Symbol>(first);
    Collections.sort(sortList);
    assertEquals("[q]", sortList.toString());

  }

  @Test
  public void testFirstWithEpsilonSymbolsAndEOI() {
    /*
    * Example from "Elizabeth Scott, Adrian Johnstone, Right Nulled GLR Parsers, 2006"

    0. S -> bBSS;
    1. S -> a;
    2. S -> ε;
    3. B -> ε;
     */
    // ε
    grammar.add(TestFixtureBuilder.createProjection("S", "bBSS"));
    grammar.add(TestFixtureBuilder.createProjection("S", "a"));
    grammar.add(TestFixtureBuilder.createProjection("S", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("B", "ε"));
    grammar.prepareGrammar("S");

    List<Symbol> args = new ArrayList<>();

    // FIRST(SS$) = {ε, a, b, $}
    Collections.addAll(args, new Symbol("S", SymbolTypes.NonTerminal));
    Collections.addAll(args, new Symbol("S", SymbolTypes.NonTerminal));
    Collections.addAll(args, Symbol.EOI);
    Collection<Symbol> first = testable.calculateFirst(args);
    List<Symbol> sortList = new ArrayList<Symbol>(first);
    Collections.sort(sortList);
    assertEquals("[a, b, EPSILON, EOI]", sortList.toString());
  }

  /*
  1. S--- > CC
  2. C--- > cC
  3. C--- > d
   */
  @Test
  public void testCalculateStates() {
    grammar.add(TestFixtureBuilder.createProjection("S", "CC"));
    grammar.add(TestFixtureBuilder.createProjection("C", "cC"));
    grammar.add(TestFixtureBuilder.createProjection("C", "d"));
    grammar.prepareGrammar();

    testable.calculateStates();
    assertEquals("[[C->* d [d], C->* d [c], C->* c C [d], C->* c C [c], S->* C C [EOI]], "
        + "[C->* d [d], C->* d [c], C->c * C [d], C->c * C [c], C->* c C [d], C->* c C [c]], "
        + "[C->d * [d], C->d * [c]], [C->* d [EOI], C->* c C [EOI], S->C * C [EOI]], "
        + "[C->c C * [d], C->c C * [c]], "
        + "[C->* d [EOI], C->c * C [EOI], C->* c C [EOI]], "
        + "[C->d * [EOI]], "
        + "[S->C C * [EOI]], "
        + "[C->c C * [EOI]]]"
        ,
        testable.getStates().toString());
  }

  @Test
  public void testCalculateStatesWithEpsilonSymbols() {
    grammar.add(TestFixtureBuilder.createProjection("T", "S"));
    grammar.add(TestFixtureBuilder.createProjection("S", "aEB"));
    grammar.add(TestFixtureBuilder.createProjection("E", "FG"));
    grammar.add(TestFixtureBuilder.createProjection("F", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("G", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("G", "g"));
    grammar.add(TestFixtureBuilder.createProjection("B", "b"));
    grammar.prepareGrammar();

    testable.calculateStates();
    assertEquals("[[S->* a E B [EOI], T->* S [EOI]], "
        + "[G->* g [b], G->EPSILON * [b], F->EPSILON * [g], F->EPSILON * [b], E->F G * [b], E->F * G [b], E->* F G [b], S->a * E B [EOI]], "
        + "[T->S * [EOI]], [G->g * [b]], "
        + "[B->* b [EOI], S->a E * B [EOI]], "
        + "[G->* g [b], G->EPSILON * [b], E->F * G [b]], "
        + "[E->F G * [b]], [B->b * [EOI]], "
        + "[S->a E B * [EOI]]]", testable.getStates().toString());

    testable.calculateRN2ActionTable();
    assertEquals("[{a=[s1], S=[s2]}, "
        + "{b=[r(G, 0, 3(4)), r(F, 0, 2(3)), r(E, 2, 0(2)), r(E, 1, 3(2)), r(E, 0, 4(2))], g=[s3, r(F, 0, 2(3))], E=[s4], F=[s5], G=[s6]}, "
        + "{EOI=[r(T, 1, 0(0))/acc]}, "
        + "{b=[r(G, 1, 0(5))]}, {b=[s7], B=[s8]}, "
        + "{b=[r(G, 0, 3(4)), r(E, 1, 3(2))], g=[s3], G=[s6]}, "
        + "{b=[r(E, 2, 0(2))]}, "
        + "{EOI=[r(B, 1, 0(6))]}, "
        + "{EOI=[r(S, 3, 0(1))]}]",
        testable.getRN2ActionTable().toString());

  }

  /*
  1. S--- > CC
  2. C--- > cC
  3. C--- > d
   */
  @Test
  public void testCalculateActionTable() {
    grammar.add(TestFixtureBuilder.createProjection("S", "CC"));
    grammar.add(TestFixtureBuilder.createProjection("C", "cC"));
    grammar.add(TestFixtureBuilder.createProjection("C", "d"));
    grammar.prepareGrammar();

    testable.calculateStates();
    testable.calculateRN2ActionTable();
    assertEquals("[{c=[s1], d=[s2], C=[s3]}, "
        + "{c=[s1], d=[s2], C=[s4]}, "
        + "{c=[r(C, 1, 0(2))], d=[r(C, 1, 0(2))]}, "
        + "{c=[s5], d=[s6], C=[s7]}, "
        + "{c=[r(C, 2, 0(1))], d=[r(C, 2, 0(1))]}, "
        + "{c=[s5], d=[s6], C=[s8]}, "
        + "{EOI=[r(C, 1, 0(2))]}, "
        + "{EOI=[r(S, 2, 0(0))/acc]}, "
        + "{EOI=[r(C, 2, 0(1))]}]"        ,
        testable.getRN2ActionTable().toString());
  }

  /*
  1. S->aAd;
  2. S->bBd;
  3. S->aBe;
  4. S->bAe;
  5. A->c;
  6. B->c;
   */
  @Test
  public void testCalculateParseTable_Ambigious() {
    grammar.add(TestFixtureBuilder.createProjection("S", "aAd"));
    grammar.add(TestFixtureBuilder.createProjection("S", "bBd"));
    grammar.add(TestFixtureBuilder.createProjection("S", "aBe"));
    grammar.add(TestFixtureBuilder.createProjection("S", "bAe"));
    grammar.add(TestFixtureBuilder.createProjection("A", "c"));
    grammar.add(TestFixtureBuilder.createProjection("B", "c"));
    grammar.prepareGrammar();

    testable.calculateStates();
    assertEquals("[[S->* b A e [EOI], S->* a B e [EOI], S->* b B d [EOI], S->* a A d [EOI]], "
        + "[B->* c [e], A->* c [d], S->a * B e [EOI], S->a * A d [EOI]], "
        + "[B->* c [d], A->* c [e], S->b * A e [EOI], S->b * B d [EOI]], "
        + "[B->c * [e], A->c * [d]], "
        + "[S->a A * d [EOI]], "
        + "[S->a B * e [EOI]], "
        + "[B->c * [d], A->c * [e]], "
        + "[S->b A * e [EOI]], "
        + "[S->b B * d [EOI]], "
        + "[S->a A d * [EOI]], "
        + "[S->a B e * [EOI]], "
        + "[S->b A e * [EOI]], "
        + "[S->b B d * [EOI]]]"
        ,
        testable.getStates().toString());

    testable.calculateRN2ActionTable();
    assertEquals(
        "[{a=[s1], b=[s2]}, {c=[s3], A=[s4], B=[s5]}, "
        + "{c=[s6], A=[s7], B=[s8]}, "
        + "{d=[r(A, 1, 0(4))], e=[r(B, 1, 0(5))]}, "
        + "{d=[s9]}, "
        + "{e=[s10]}, "
        + "{d=[r(B, 1, 0(5))], e=[r(A, 1, 0(4))]}, "
        + "{e=[s11]}, "
        + "{d=[s12]}, "
        + "{EOI=[r(S, 3, 0(0))/acc]}, "
        + "{EOI=[r(S, 3, 0(2))/acc]}, "
        + "{EOI=[r(S, 3, 0(3))/acc]}, "
        + "{EOI=[r(S, 3, 0(1))/acc]}]",
        testable.getRN2ActionTable().toString());
  }

  /*
  1. S->E;
  2. E->E+E;
  3. E->d;
   */
  @Test
  public void testCalculateParseTable_Ambigious2() {
    grammar.add(TestFixtureBuilder.createProjection("S", "E"));
    grammar.add(TestFixtureBuilder.createProjection("E", "E+E"));
    grammar.add(TestFixtureBuilder.createProjection("E", "d"));
    grammar.prepareGrammar();

    testable.calculateStates();
    assertEquals(5, testable.getStates().size());

    assertEquals(
        "[[E->* d [EOI], E->* d [+], E->* E + E [EOI], E->* E + E [+], S->* E [EOI]], "
            + "[E->d * [EOI], E->d * [+]], "
            + "[E->E * + E [EOI], E->E * + E [+], S->E * [EOI]], "
            + "[E->* d [EOI], E->* d [+], E->E + * E [EOI], E->E + * E [+], E->* E + E [EOI], E->* E + E [+]], "
            + "[E->E + E * [EOI], E->E + E * [+], E->E * + E [EOI], E->E * + E [+]]]",
        testable.getStates().toString());

    testable.calculateRN2ActionTable();
    assertEquals("[{d=[s1], E=[s2]}, "
        + "{+=[r(E, 1, 0(2))], EOI=[r(E, 1, 0(2))]}, "
        + "{+=[s3], EOI=[r(S, 1, 0(0))/acc]}, "
        + "{d=[s1], E=[s4]}, "
        + "{+=[s3, r(E, 3, 0(1))], EOI=[r(E, 3, 0(1))]}]",
        testable.getRN2ActionTable().toString());
  }

  /*
   * 1. S ---> aSA 
   * 2. S ---> ε 
   * 3. A ---> ε
   */
  @Test
  public void testCalculateParseTable_Epsilon() {
    grammar.add(TestFixtureBuilder.createProjection("T", "S"));
    grammar.add(TestFixtureBuilder.createProjection("S", "aSA"));
    grammar.add(TestFixtureBuilder.createProjection("S", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("A", "ε"));
    grammar.prepareGrammar();

    testable.calculateStates();
    //
    assertEquals("[S->EPSILON * [EOI], S->* a S A [EOI], T->* S [EOI]]",
        testable.getStates().get(0).toString());
    
    assertEquals("[S->EPSILON * [EOI], S->a * S A [EOI], S->* a S A [EOI]]",
        testable.getStates().get(1).toString());
    assertEquals("[T->S * [EOI]]", testable.getStates().get(2).toString());
    assertEquals("[S->a S A * [EOI]]", testable.getStates().get(4).toString());

    assertEquals("[0-(a)->1, 0-(S)->2, 1-(a)->1, 1-(S)->3, 3-(A)->4]",
        testable.getStateTransitions().toString());

    testable.calculateRN2ActionTable();
    assertEquals("[{a=[s1], S=[s2], EOI=[r(S, 0, 2(2)), r(T, 0, 2(0))]}, "
        + "{a=[s1], S=[s3], EOI=[r(S, 0, 2(2)), r(S, 1, 4(1))]}, "
        + "{EOI=[r(T, 1, 0(0))/acc]}, "
        + "{A=[s4], EOI=[r(A, 0, 1(3)), r(S, 2, 1(1))]}, "
        + "{EOI=[r(S, 3, 0(1))]}]",
        testable.getRN2ActionTable().toString());
  }

  /*
  S -> .*NA.*
  F is T0, L is T1, T is any_token.

  0. S -> F;
  1. F -> NAL;
  2. F -> TF;
  3. L -> ε;
  4. L-> TL;
   */
  @Test
  public void testCalculateParseTable_RegexpEpsilon() {
    grammar.add(TestFixtureBuilder.createProjection("S", "F"));
    grammar.add(TestFixtureBuilder.createProjection("F", "naL"));
    grammar.add(TestFixtureBuilder.createProjection("F", "tF"));
    grammar.add(TestFixtureBuilder.createProjection("L", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("L", "tL"));
    grammar.prepareGrammar();

    testable.calculateStates();
    //
    assertEquals(
        "[[F->* t F [EOI], F->* n a L [EOI], S->* F [EOI]], "
        + "[F->n * a L [EOI]], "
        + "[F->t * F [EOI], F->* t F [EOI], F->* n a L [EOI]], "
        + "[S->F * [EOI]], "
        + "[L->* t L [EOI], L->EPSILON * [EOI], F->n a * L [EOI]], "
        + "[F->t F * [EOI]], "
        + "[L->t * L [EOI], L->* t L [EOI], L->EPSILON * [EOI]], "
        + "[F->n a L * [EOI]], "
        + "[L->t L * [EOI]]]",
        testable.getStates().toString());

    assertEquals("[0-(n)->1, 0-(t)->2, 0-(F)->3, 1-(a)->4, 2-(n)->1, 2-(t)->2, 2-(F)->5, 4-(t)->6, 4-(L)->7, 6-(t)->6, 6-(L)->8]"
        ,
        testable.getStateTransitions().toString());

    testable.calculateRN2ActionTable();
    assertEquals("[{n=[s1], t=[s2], F=[s3]}, "
        + "{a=[s4]}, "
        + "{n=[s1], t=[s2], F=[s5]}, "
        + "{EOI=[r(S, 1, 0(0))/acc]}, "
        + "{t=[s6], L=[s7], EOI=[r(L, 0, 1(3)), r(F, 2, 1(1))]}, "
        + "{EOI=[r(F, 2, 0(2))]}, "
        + "{t=[s6], L=[s8], EOI=[r(L, 1, 1(4)), r(L, 0, 1(3))]}, "
        + "{EOI=[r(F, 3, 0(1))]}, "
        + "{EOI=[r(L, 2, 0(4))]}]",
        testable.getRN2ActionTable().toString());

  }

  /*
  S -> .*NA.*
  F is T0, L is T1, T is any_token.

  0. S -> F;
  1. F -> NAL;
  2. F -> TF;
  3. L -> ε;
  4. L-> TL;
   */
  @Test
  public void testCreateRNReductoins() {
    grammar.add(TestFixtureBuilder.createProjection("S", "F"));
    grammar.add(TestFixtureBuilder.createProjection("F", "naL"));
    grammar.add(TestFixtureBuilder.createProjection("F", "tF"));
    grammar.add(TestFixtureBuilder.createProjection("L", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("L", "tL"));
    grammar.prepareGrammar();

    testable.calculateStates();
    Map<Symbol, List<ActionRecord>> actionTableEntry =
        new TreeMap<Symbol, List<ActionRecord>>();

    actionTableEntry.clear();
    assertEquals("[L->* t L [EOI], L->EPSILON * [EOI], F->n a * L [EOI]]",
        testable.getStates().get(4).toString());
    testable.calculateRNReductions(actionTableEntry,
        testable.getStates().get(4));
    assertEquals("{EOI=[r(L, 0), r(F, 2)]}", actionTableEntry.toString());

    actionTableEntry.clear();
    assertEquals("[F->t F * [EOI]]", testable.getStates().get(5).toString());
    testable.calculateRNReductions(actionTableEntry,
        testable.getStates().get(5));
    assertEquals("{EOI=[r(F, 2)]}", actionTableEntry.toString());

    actionTableEntry.clear();
    assertEquals("[L->t * L [EOI], L->* t L [EOI], L->EPSILON * [EOI]]", testable.getStates().get(6).toString());
    testable.calculateRNReductions(actionTableEntry,
        testable.getStates().get(6));
    assertEquals("{EOI=[r(L, 1), r(L, 0)]}", actionTableEntry.toString());

    actionTableEntry.clear();
    assertEquals("[F->n a L * [EOI]]",
        testable.getStates().get(7).toString());
    testable.calculateRNReductions(actionTableEntry,
        testable.getStates().get(7));
    assertEquals("{EOI=[r(F, 3)]}", actionTableEntry.toString());
  }

  /*
   * Example from "Elizabeth Scott, Adrian Johnstone, Right Nulled GLR Parsers, 2006"

  0. S -> aSA;
  1. S -> ε;
  2. A -> ε;
   */
  @Test
  public void testCreateRNActionTable() {
    grammar.add(TestFixtureBuilder.createProjection("S", "aSA"));
    grammar.add(TestFixtureBuilder.createProjection("S", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("A", "ε"));
    grammar.prepareGrammar("S");

    testable.calculateStates();
    testable.calculateRN2ActionTable();

    assertEquals("[{a=[s1], EOI=[r(S, 0, 2(1))/acc]}, "
        + "{a=[s1], S=[s2], EOI=[r(S, 0, 2(1))/acc, r(S, 1, 3(0))]}, "
        + "{A=[s3], EOI=[r(A, 0, 1(2)), r(S, 2, 1(0))]}, "
        + "{EOI=[r(S, 3, 0(0))/acc]}]",
        testable.getRN2ActionTable().toString());
  }

  @Test
  public void testIsNullable() {
    /*
     * Example from
    Example from "Parsing Techniques: A Practical Guide by Dick Grune, Ceriel J. Jacobs"
      
    S ---> A | AB | B 
    A ---> C 
    B ---> D 
    C ---> p | ε
    D ---> q 
     */
    // ε
    grammar.add(TestFixtureBuilder.createProjection("S", "A"));
    grammar.add(TestFixtureBuilder.createProjection("S", "B"));
    grammar.add(TestFixtureBuilder.createProjection("S", "AB"));
    grammar.add(TestFixtureBuilder.createProjection("A", "C"));
    grammar.add(TestFixtureBuilder.createProjection("B", "D"));
    grammar.add(TestFixtureBuilder.createProjection("C", "p"));
    grammar.add(TestFixtureBuilder.createProjection("C", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("D", "q"));
    grammar.prepareGrammar();

    List<Symbol> args = new ArrayList<>();

    // isNullable(AB) == false
    args.clear();
    Collections.addAll(args, new Symbol("A", SymbolTypes.NonTerminal));
    Collections.addAll(args, new Symbol("B", SymbolTypes.NonTerminal));
    assertFalse(testable.isNullable(args));

    // isNullable(C) == true
    args.clear();
    Collections.addAll(args, new Symbol("C", SymbolTypes.NonTerminal));
    assertTrue(testable.isNullable(args));

    // isNullable(SSS) == true (because there is "S->A->C->ε")
    args.clear();
    Collections.addAll(args, new Symbol("S", SymbolTypes.NonTerminal));
    Collections.addAll(args, new Symbol("S", SymbolTypes.NonTerminal));
    Collections.addAll(args, new Symbol("S", SymbolTypes.NonTerminal));
    assertTrue(testable.isNullable(args));

    // isNullable(SaS) == false
    args.clear();
    Collections.addAll(args, new Symbol("S", SymbolTypes.NonTerminal));
    Collections.addAll(args, new Symbol("a", SymbolTypes.Terminal));
    Collections.addAll(args, new Symbol("S", SymbolTypes.NonTerminal));
    assertFalse(testable.isNullable(args));

    // isNullable(SεS) == true
    args.clear();
    Collections.addAll(args, new Symbol("S", SymbolTypes.NonTerminal));
    Collections.addAll(args, Symbol.EPSILON);
    Collections.addAll(args, new Symbol("S", SymbolTypes.NonTerminal));
    assertTrue(testable.isNullable(args));

    // isNullable(ε) == true
    args.clear();
    Collections.addAll(args, Symbol.EPSILON);
    assertTrue(testable.isNullable(args));

    // isNullable(NONE) == false
    args.clear();
    assertTrue(testable.isNullable(args));

  }

  /*
   * Example from "Elizabeth Scott, Adrian Johnstone, Right Nulled GLR Parsers, 2006"

  0. S -> bBSS;
  1. S -> a;
  2. S -> ε;
  3. B -> ε;
  
  states:
  [
  0 [S->*EPSILON [EOI], S->*a [EOI], S->*bBSS [EOI]], 
  1 [S->a* [EOI]], 
  2 [B->EPSILON* [EOI], B->EPSILON* [b], B->EPSILON* [a], S->b*BSS [EOI]], 
  3 [S->EPSILON* [EOI], S->EPSILON* [b], S->EPSILON* [a], S->*a [EOI], S->*a [b], S->*a [a], S->bB*SS [EOI], S->*bBSS [EOI], S->*bBSS [b], S->*bBSS [a]], 
  4 [S->EPSILON* [EOI], S->*a [EOI], S->bBS*S [EOI], S->*bBSS [EOI]], 
  5 [S->a* [EOI], S->a* [b], S->a* [a]], 
  6 [B->EPSILON* [EOI], B->EPSILON* [b], B->EPSILON* [a], S->b*BSS [EOI], S->b*BSS [b], S->b*BSS [a]], 
  7 [S->bBSS* [EOI]], 
  8 [S->EPSILON* [EOI], S->EPSILON* [b], S->EPSILON* [a], S->*a [EOI], S->*a [b], S->*a [a], S->bB*SS [EOI], S->bB*SS [b], S->bB*SS [a], S->*bBSS [EOI], S->*bBSS [b], S->*bBSS [a]], 
  9 [S->EPSILON* [EOI], S->EPSILON* [b], S->EPSILON* [a], S->*a [EOI], S->*a [b], S->*a [a], S->bBS*S [EOI], S->bBS*S [b], S->bBS*S [a], S->*bBSS [EOI], S->*bBSS [b], S->*bBSS [a]], 
  10 [S->bBSS* [EOI], S->bBSS* [b], S->bBSS* [a]]
  ]
  
  actions:
  [
  0 {a=[s1], b=[s2], EOI=[r(S, 0, 1)/acc]}, 
  1 {EOI=[r(S, 1, 0)/acc]}, 
  2 {B=[s3], a=[r(B, 0, 2)], b=[r(B, 0, 2)], EOI=[r(B, 0, 2), r(S, 1, 4)]}, 
  3 {S=[s4], a=[s5, r(S, 0, 1)/acc], b=[s6, r(S, 0, 1)/acc], EOI=[r(S, 0, 1)/acc, r(S, 2, 3)]}, 
  4 {S=[s7], a=[s1], b=[s2], EOI=[r(S, 0, 1)/acc, r(S, 3, 1)]}, 
  5 {a=[r(S, 1, 0)/acc], b=[r(S, 1, 0)/acc], EOI=[r(S, 1, 0)/acc]}, 
  6 {B=[s8], a=[r(B, 0, 2), r(S, 1, 4)], b=[r(B, 0, 2), r(S, 1, 4)], EOI=[r(B, 0, 2), r(S, 1, 4)]}, 
  7 {EOI=[r(S, 4, 0)/acc]}, 
  8 {S=[s9], a=[s5, r(S, 0, 1)/acc, r(S, 2, 3)], b=[s6, r(S, 0, 1)/acc, r(S, 2, 3)], EOI=[r(S, 0, 1)/acc, r(S, 2, 3)]}, 
  9 {S=[s10], a=[s5, r(S, 0, 1)/acc, r(S, 3, 1)], b=[s6, r(S, 0, 1)/acc, r(S, 3, 1)], EOI=[r(S, 0, 1)/acc, r(S, 3, 1)]}, 
  10 {a=[r(S, 4, 0)/acc], b=[r(S, 4, 0)/acc], EOI=[r(S, 4, 0)/acc]}
  ]
     */
  @Test
  public void testCreateRN2ActionTable() {
    grammar.add(TestFixtureBuilder.createProjection("S", "bBSS"));
    grammar.add(TestFixtureBuilder.createProjection("S", "a"));
    grammar.add(TestFixtureBuilder.createProjection("S", "ε"));
    grammar.add(TestFixtureBuilder.createProjection("B", "ε"));
    grammar.prepareGrammar("S");

    testable.calculateStates();
    testable.calculateRN2ActionTable();

    assertEquals("[[S->EPSILON * [EOI], S->* a [EOI], S->* b B S S [EOI]], "
        + "[S->a * [EOI]], [B->EPSILON * [EOI], B->EPSILON * [b], B->EPSILON * [a], S->b * B S S [EOI]], "
        + "[S->EPSILON * [EOI], S->EPSILON * [b], S->EPSILON * [a], S->* a [EOI], S->* a [b], S->* a [a], S->b B * S S [EOI], S->* b B S S [EOI], S->* b B S S [b], S->* b B S S [a]], "
        + "[S->a * [EOI], S->a * [b], S->a * [a]], "
        + "[B->EPSILON * [EOI], B->EPSILON * [b], B->EPSILON * [a], S->b * B S S [EOI], S->b * B S S [b], S->b * B S S [a]], "
        + "[S->EPSILON * [EOI], S->* a [EOI], S->b B S * S [EOI], S->* b B S S [EOI]], "
        + "[S->EPSILON * [EOI], S->EPSILON * [b], S->EPSILON * [a], S->* a [EOI], S->* a [b], S->* a [a], S->b B * S S [EOI], S->b B * S S [b], S->b B * S S [a], S->* b B S S [EOI], S->* b B S S [b], S->* b B S S [a]], "
        + "[S->b B S S * [EOI]], "
        + "[S->EPSILON * [EOI], S->EPSILON * [b], S->EPSILON * [a], S->* a [EOI], S->* a [b], S->* a [a], S->b B S * S [EOI], S->b B S * S [b], S->b B S * S [a], S->* b B S S [EOI], S->* b B S S [b], S->* b B S S [a]], "
        + "[S->b B S S * [EOI], S->b B S S * [b], S->b B S S * [a]]]",
        testable.getStates().toString());

    assertEquals("[{a=[s1], b=[s2], EOI=[r(S, 0, 2(2))/acc]}, "
        + "{EOI=[r(S, 1, 0(1))/acc]}, "
        + "{a=[r(B, 0, 1(3))], b=[r(B, 0, 1(3))], B=[s3], EOI=[r(B, 0, 1(3)), r(S, 1, 4(0))]}, "
        + "{a=[s4, r(S, 0, 2(2))/acc], b=[s5, r(S, 0, 2(2))/acc], S=[s6], EOI=[r(S, 0, 2(2))/acc, r(S, 2, 3(0))]}, "
        + "{a=[r(S, 1, 0(1))/acc], b=[r(S, 1, 0(1))/acc], EOI=[r(S, 1, 0(1))/acc]}, "
        + "{a=[r(B, 0, 1(3)), r(S, 1, 4(0))], b=[r(B, 0, 1(3)), r(S, 1, 4(0))], B=[s7], EOI=[r(B, 0, 1(3)), r(S, 1, 4(0))]}, "
        + "{a=[s1], b=[s2], S=[s8], EOI=[r(S, 0, 2(2))/acc, r(S, 3, 2(0))]}, "
        + "{a=[s4, r(S, 0, 2(2))/acc, r(S, 2, 3(0))], b=[s5, r(S, 0, 2(2))/acc, r(S, 2, 3(0))], S=[s9], EOI=[r(S, 0, 2(2))/acc, r(S, 2, 3(0))]}, "
        + "{EOI=[r(S, 4, 0(0))/acc]}, "
        + "{a=[s4, r(S, 0, 2(2))/acc, r(S, 3, 2(0))], b=[s5, r(S, 0, 2(2))/acc, r(S, 3, 2(0))], S=[s10], EOI=[r(S, 0, 2(2))/acc, r(S, 3, 2(0))]}, "
        + "{a=[r(S, 4, 0(0))/acc], b=[r(S, 4, 0(0))/acc], EOI=[r(S, 4, 0(0))/acc]}]",
        testable.getRN2ActionTable().toString());
  }

  @Test
  public void testCreateRN2ActionTable_WithSymbolExt() {
    String grammarText =
        "ProperName ->  Word<h-reg1>; \n" 
            + "ProperName ->  Word<h-reg2>;\n"
            + "Person -> (ProperName |'человек');";

    Grammar grammar2 = TestFixtureBuilder.parseGrammar(grammarText, "Person");

    grammar2.getProjections().forEach((p) -> grammar.add(p));
    grammar.prepareGrammar("Person");

    testable.calculateStates();
    testable.calculateRN2ActionTable();

    assertEquals(
        "[[Person->* 'человек' [EOI], Person->* ProperName [EOI], ProperName->* word<h-reg2> [EOI], ProperName->* word<h-reg1> [EOI]], "
            + "[ProperName->word<h-reg1> * [EOI]], "
            + "[ProperName->word<h-reg2> * [EOI]], "
            + "[Person->'человек' * [EOI]], " + "[Person->ProperName * [EOI]]]",
        testable.getStates().toString());

    assertEquals(
        "[{word<h-reg1>=[s1], word<h-reg2>=[s2], 'человек'=[s3], ProperName=[s4]}, "
            + "{EOI=[r(ProperName, 1, 0(0))]}, "
            + "{EOI=[r(ProperName, 1, 0(1))]}, "
            + "{EOI=[r(Person, 1, 0(3))/acc]}, " + "{EOI=[r(Person, 1, 0(2))/acc]}]",
        testable.getRN2ActionTable().toString());
  }

  
  
  @Test
  // @Ignore
  public void testCreateRN2ActionTable_WithSymbolExt2() throws IOException {
    String grammarText ="S->Ar<h-reg1>;"+
        "Ar->verb<rt, gnc-agr=[1]> noun<gnc-agr=[1]>;";
    Grammar grammar2 = TestFixtureBuilder.parseGrammar(grammarText, "S");
    grammar2.getProjections().forEach((p) -> grammar.add(p));
    grammar.prepareGrammar("S");
   
    testable.calculateStates();
    testable.calculateRN2ActionTable();


    assertEquals("[[Ar->* verb<rt, gnc-agr=[1]> noun<gnc-agr=[1]> [EOI], S->* Ar<h-reg1> [EOI]], "
        + "[Ar->verb<rt, gnc-agr=[1]> * noun<gnc-agr=[1]> [EOI]], "
        + "[S->Ar<h-reg1> * [EOI]], "
        + "[Ar->verb<rt, gnc-agr=[1]> noun<gnc-agr=[1]> * [EOI]]]",
        testable.getStates().toString());

    assertEquals("[{verb<rt, gnc-agr=[1]>=[s1], Ar<h-reg1>=[s2]}, "
        + "{noun<gnc-agr=[1]>=[s3]}, "
        + "{EOI=[r(S, 1, 0(0))/acc]}, "
        + "{EOI=[r(Ar, 2, 0(1))]}]",
        testable.getRN2ActionTable().toString());

    assertEquals("[0-(verb<rt, gnc-agr=[1]>)->1, "
        + "0-(Ar<h-reg1>)->2, "
        + "1-(noun<gnc-agr=[1]>)->3]",
        testable.getStateTransitions().toString());

  }

  
  /**
   * Проверка корректного определения нетермениала при 2-х его определениях.
   * Ar->verb<rt, gnc-agr=[1]> noun<gnc-agr=[1]>;
   * Ar->verb<rt, gnc-agr=[1]> noun<gnc-agr=[1]> adj;"
        
   * @throws IOException
   */
  @Test
  public void testCreateRN2ActionTable_WithSymbolExt3() throws IOException {
    String grammarText ="S->Ar<h-reg1>;"
        + "Ar->verb<rt, gnc-agr=[1]> noun<gnc-agr=[1]>;"
        + "Ar->verb<rt, gnc-agr=[1]> noun<gnc-agr=[1]> adj;";
    Grammar grammar2 = TestFixtureBuilder.parseGrammar(grammarText, "S");
    grammar2.getProjections().forEach((p) -> grammar.add(p));
    grammar.prepareGrammar("S");
   
    testable.calculateStates();
    testable.calculateRN2ActionTable();


    assertEquals("[[Ar->* verb<rt, gnc-agr=[1]> noun<gnc-agr=[1]> adj [EOI], Ar->* verb<rt, gnc-agr=[1]> noun<gnc-agr=[1]> [EOI], S->* Ar<h-reg1> [EOI]], "
        + "[Ar->verb<rt, gnc-agr=[1]> * noun<gnc-agr=[1]> adj [EOI], Ar->verb<rt, gnc-agr=[1]> * noun<gnc-agr=[1]> [EOI]], "
        + "[S->Ar<h-reg1> * [EOI]], "
        + "[Ar->verb<rt, gnc-agr=[1]> noun<gnc-agr=[1]> * adj [EOI], Ar->verb<rt, gnc-agr=[1]> noun<gnc-agr=[1]> * [EOI]], "
        + "[Ar->verb<rt, gnc-agr=[1]> noun<gnc-agr=[1]> adj * [EOI]]]",
        testable.getStates().toString());

    assertEquals("[{verb<rt, gnc-agr=[1]>=[s1], Ar<h-reg1>=[s2]}, "
        + "{noun<gnc-agr=[1]>=[s3]}, "
        + "{EOI=[r(S, 1, 0(0))/acc]}, "
        + "{adj=[s4], EOI=[r(Ar, 2, 0(1))]}, "
        + "{EOI=[r(Ar, 3, 0(2))]}]",
        testable.getRN2ActionTable().toString());

    assertEquals("[0-(verb<rt, gnc-agr=[1]>)->1, "
        + "0-(Ar<h-reg1>)->2, "
        + "1-(noun<gnc-agr=[1]>)->3, "
        + "3-(adj)->4]",
        testable.getStateTransitions().toString());
    
    assertEquals("[{verb=[verb<rt, gnc-agr=[1]>], Ar=[Ar<h-reg1>]}, "
        + "{noun=[noun<gnc-agr=[1]>]}, "
        + "{}, "
        + "{adj=[adj]}, "
        + "{}]",
        testable.getBaseNTToNTExtData().toString());

  }
  
  @Test
  //@Ignore
  public void testProcessYandexGettingStarted_Ex2_Txt3() throws IOException {
    String grammarText ="ProperName ->  Word<h-reg1>+;"+
        "ProperName ->  Word<h-reg2>+;"+
        "Person -> (ProperName |'человек');"+
        "FormOfAddress -> ('товарищ' | 'мистер' | 'господин');"+
        "AdjCoord -> Adj;"+
        "AdjCoord -> AdjCoord<gnc-agr=[1]> 'или' Adj<gnc-agr=[1]>;"+
        "AdjCoord -> AdjCoord<gnc-agr=[1]> 'и' Adj<gnc-agr=[1]>;"+
        "S -> Adj<gnc-agr=[1]>+ (FormOfAddress)? Person<gnc-agr=[1]>;"+ 
        "S -> AdjCoord<gnc-agr=[1]> (FormOfAddress)? Person<gnc-agr=[1]>;"+
        "Root->S;";
    Grammar grammar2 = TestFixtureBuilder.parseGrammar(grammarText, "Root");
    grammar2.getProjections().forEach((p) -> grammar.add(p));
    grammar.prepareGrammar("Root");
    
    testable.calculateStates();
    testable.calculateRN2ActionTable();

    assertEquals("[[S->* AdjCoord<gnc-agr=[1]> FormOfAddress Person<gnc-agr=[1]> [EOI], S->* adj<gnc-agr=[1]> T_2 [EOI], S->* AdjCoord<gnc-agr=[1]> Person<gnc-agr=[1]> [EOI], Root->* S [EOI], AdjCoord->* AdjCoord<gnc-agr=[1]> 'и' adj<gnc-agr=[1]> ['человек'], AdjCoord->* AdjCoord<gnc-agr=[1]> 'и' adj<gnc-agr=[1]> ['товарищ'], AdjCoord->* AdjCoord<gnc-agr=[1]> 'и' adj<gnc-agr=[1]> ['мистер'], AdjCoord->* AdjCoord<gnc-agr=[1]> 'и' adj<gnc-agr=[1]> ['или'], AdjCoord->* AdjCoord<gnc-agr=[1]> 'и' adj<gnc-agr=[1]> ['и'], AdjCoord->* AdjCoord<gnc-agr=[1]> 'и' adj<gnc-agr=[1]> ['господин'], AdjCoord->* AdjCoord<gnc-agr=[1]> 'и' adj<gnc-agr=[1]> [word<h-reg2>], AdjCoord->* AdjCoord<gnc-agr=[1]> 'и' adj<gnc-agr=[1]> [word<h-reg1>], AdjCoord->* AdjCoord<gnc-agr=[1]> 'или' adj<gnc-agr=[1]> ['человек'], AdjCoord->* AdjCoord<gnc-agr=[1]> 'или' adj<gnc-agr=[1]> ['товарищ'], AdjCoord->* AdjCoord<gnc-agr=[1]> 'или' adj<gnc-agr=[1]> ['мистер'], AdjCoord->* AdjCoord<gnc-agr=[1]> 'или' adj<gnc-agr=[1]> ['или'], AdjCoord->* AdjCoord<gnc-agr=[1]> 'или' adj<gnc-agr=[1]> ['и'], AdjCoord->* AdjCoord<gnc-agr=[1]> 'или' adj<gnc-agr=[1]> ['господин'], AdjCoord->* AdjCoord<gnc-agr=[1]> 'или' adj<gnc-agr=[1]> [word<h-reg2>], AdjCoord->* AdjCoord<gnc-agr=[1]> 'или' adj<gnc-agr=[1]> [word<h-reg1>], AdjCoord->* adj ['человек'], AdjCoord->* adj ['товарищ'], AdjCoord->* adj ['мистер'], AdjCoord->* adj ['или'], AdjCoord->* adj ['и'], AdjCoord->* adj ['господин'], AdjCoord->* adj [word<h-reg2>], AdjCoord->* adj [word<h-reg1>]], "
        + "[AdjCoord->adj * ['человек'], AdjCoord->adj * ['товарищ'], AdjCoord->adj * ['мистер'], AdjCoord->adj * ['или'], AdjCoord->adj * ['и'], AdjCoord->adj * ['господин'], AdjCoord->adj * [word<h-reg2>], AdjCoord->adj * [word<h-reg1>]], "
        + "[T_2->* FormOfAddress Person<gnc-agr=[1]> [EOI], T_2->* Person<gnc-agr=[1]> [EOI], T_2->* adj<gnc-agr=[1]> T_2 [EOI], S->adj<gnc-agr=[1]> * T_2 [EOI], ProperName->* word<h-reg2> T_1 [EOI], ProperName->* word<h-reg1> T_0 [EOI], FormOfAddress->* 'господин' ['человек'], FormOfAddress->* 'господин' [word<h-reg2>], FormOfAddress->* 'господин' [word<h-reg1>], FormOfAddress->* 'мистер' ['человек'], FormOfAddress->* 'мистер' [word<h-reg2>], FormOfAddress->* 'мистер' [word<h-reg1>], FormOfAddress->* 'товарищ' ['человек'], FormOfAddress->* 'товарищ' [word<h-reg2>], FormOfAddress->* 'товарищ' [word<h-reg1>], Person->* 'человек' [EOI], Person->* ProperName [EOI]], "
        + "[S->AdjCoord<gnc-agr=[1]> * FormOfAddress Person<gnc-agr=[1]> [EOI], ProperName->* word<h-reg2> T_1 [EOI], ProperName->* word<h-reg1> T_0 [EOI], S->AdjCoord<gnc-agr=[1]> * Person<gnc-agr=[1]> [EOI], FormOfAddress->* 'господин' ['человек'], FormOfAddress->* 'господин' [word<h-reg2>], FormOfAddress->* 'господин' [word<h-reg1>], FormOfAddress->* 'мистер' ['человек'], FormOfAddress->* 'мистер' [word<h-reg2>], FormOfAddress->* 'мистер' [word<h-reg1>], FormOfAddress->* 'товарищ' ['человек'], FormOfAddress->* 'товарищ' [word<h-reg2>], FormOfAddress->* 'товарищ' [word<h-reg1>], Person->* 'человек' [EOI], Person->* ProperName [EOI], AdjCoord->AdjCoord<gnc-agr=[1]> * 'и' adj<gnc-agr=[1]> ['человек'], AdjCoord->AdjCoord<gnc-agr=[1]> * 'и' adj<gnc-agr=[1]> ['товарищ'], AdjCoord->AdjCoord<gnc-agr=[1]> * 'и' adj<gnc-agr=[1]> ['мистер'], AdjCoord->AdjCoord<gnc-agr=[1]> * 'и' adj<gnc-agr=[1]> ['или'], AdjCoord->AdjCoord<gnc-agr=[1]> * 'и' adj<gnc-agr=[1]> ['и'], AdjCoord->AdjCoord<gnc-agr=[1]> * 'и' adj<gnc-agr=[1]> ['господин'], AdjCoord->AdjCoord<gnc-agr=[1]> * 'и' adj<gnc-agr=[1]> [word<h-reg2>], AdjCoord->AdjCoord<gnc-agr=[1]> * 'и' adj<gnc-agr=[1]> [word<h-reg1>], AdjCoord->AdjCoord<gnc-agr=[1]> * 'или' adj<gnc-agr=[1]> ['человек'], AdjCoord->AdjCoord<gnc-agr=[1]> * 'или' adj<gnc-agr=[1]> ['товарищ'], AdjCoord->AdjCoord<gnc-agr=[1]> * 'или' adj<gnc-agr=[1]> ['мистер'], AdjCoord->AdjCoord<gnc-agr=[1]> * 'или' adj<gnc-agr=[1]> ['или'], AdjCoord->AdjCoord<gnc-agr=[1]> * 'или' adj<gnc-agr=[1]> ['и'], AdjCoord->AdjCoord<gnc-agr=[1]> * 'или' adj<gnc-agr=[1]> ['господин'], AdjCoord->AdjCoord<gnc-agr=[1]> * 'или' adj<gnc-agr=[1]> [word<h-reg2>], AdjCoord->AdjCoord<gnc-agr=[1]> * 'или' adj<gnc-agr=[1]> [word<h-reg1>]], "
        + "[Root->S * [EOI]], "
        + "[T_2->* FormOfAddress Person<gnc-agr=[1]> [EOI], T_2->* Person<gnc-agr=[1]> [EOI], T_2->adj<gnc-agr=[1]> * T_2 [EOI], T_2->* adj<gnc-agr=[1]> T_2 [EOI], ProperName->* word<h-reg2> T_1 [EOI], ProperName->* word<h-reg1> T_0 [EOI], FormOfAddress->* 'господин' ['человек'], FormOfAddress->* 'господин' [word<h-reg2>], FormOfAddress->* 'господин' [word<h-reg1>], FormOfAddress->* 'мистер' ['человек'], FormOfAddress->* 'мистер' [word<h-reg2>], FormOfAddress->* 'мистер' [word<h-reg1>], FormOfAddress->* 'товарищ' ['человек'], FormOfAddress->* 'товарищ' [word<h-reg2>], FormOfAddress->* 'товарищ' [word<h-reg1>], Person->* 'человек' [EOI], Person->* ProperName [EOI]], "
        + "[T_0->* word<h-reg1> T_0 [EOI], ProperName->word<h-reg1> * T_0 [EOI], T_0->EPSILON * [EOI]], "
        + "[T_1->* word<h-reg2> T_1 [EOI], ProperName->word<h-reg2> * T_1 [EOI], T_1->EPSILON * [EOI]], "
        + "[FormOfAddress->'господин' * ['человек'], FormOfAddress->'господин' * [word<h-reg2>], FormOfAddress->'господин' * [word<h-reg1>]], "
        + "[FormOfAddress->'мистер' * ['человек'], FormOfAddress->'мистер' * [word<h-reg2>], FormOfAddress->'мистер' * [word<h-reg1>]], "
        + "[FormOfAddress->'товарищ' * ['человек'], FormOfAddress->'товарищ' * [word<h-reg2>], FormOfAddress->'товарищ' * [word<h-reg1>]], "
        + "[Person->'человек' * [EOI]], "
        + "[T_2->FormOfAddress * Person<gnc-agr=[1]> [EOI], ProperName->* word<h-reg2> T_1 [EOI], ProperName->* word<h-reg1> T_0 [EOI], Person->* 'человек' [EOI], Person->* ProperName [EOI]], "
        + "[T_2->Person<gnc-agr=[1]> * [EOI]], [Person->ProperName * [EOI]], "
        + "[S->adj<gnc-agr=[1]> T_2 * [EOI]], "
        + "[AdjCoord->AdjCoord<gnc-agr=[1]> 'и' * adj<gnc-agr=[1]> ['человек'], AdjCoord->AdjCoord<gnc-agr=[1]> 'и' * adj<gnc-agr=[1]> ['товарищ'], AdjCoord->AdjCoord<gnc-agr=[1]> 'и' * adj<gnc-agr=[1]> ['мистер'], AdjCoord->AdjCoord<gnc-agr=[1]> 'и' * adj<gnc-agr=[1]> ['или'], AdjCoord->AdjCoord<gnc-agr=[1]> 'и' * adj<gnc-agr=[1]> ['и'], AdjCoord->AdjCoord<gnc-agr=[1]> 'и' * adj<gnc-agr=[1]> ['господин'], AdjCoord->AdjCoord<gnc-agr=[1]> 'и' * adj<gnc-agr=[1]> [word<h-reg2>], AdjCoord->AdjCoord<gnc-agr=[1]> 'и' * adj<gnc-agr=[1]> [word<h-reg1>]], "
        + "[AdjCoord->AdjCoord<gnc-agr=[1]> 'или' * adj<gnc-agr=[1]> ['человек'], AdjCoord->AdjCoord<gnc-agr=[1]> 'или' * adj<gnc-agr=[1]> ['товарищ'], AdjCoord->AdjCoord<gnc-agr=[1]> 'или' * adj<gnc-agr=[1]> ['мистер'], AdjCoord->AdjCoord<gnc-agr=[1]> 'или' * adj<gnc-agr=[1]> ['или'], AdjCoord->AdjCoord<gnc-agr=[1]> 'или' * adj<gnc-agr=[1]> ['и'], AdjCoord->AdjCoord<gnc-agr=[1]> 'или' * adj<gnc-agr=[1]> ['господин'], AdjCoord->AdjCoord<gnc-agr=[1]> 'или' * adj<gnc-agr=[1]> [word<h-reg2>], AdjCoord->AdjCoord<gnc-agr=[1]> 'или' * adj<gnc-agr=[1]> [word<h-reg1>]], "
        + "[S->AdjCoord<gnc-agr=[1]> FormOfAddress * Person<gnc-agr=[1]> [EOI], ProperName->* word<h-reg2> T_1 [EOI], ProperName->* word<h-reg1> T_0 [EOI], Person->* 'человек' [EOI], Person->* ProperName [EOI]], "
        + "[S->AdjCoord<gnc-agr=[1]> Person<gnc-agr=[1]> * [EOI]], "
        + "[T_2->adj<gnc-agr=[1]> T_2 * [EOI]], "
        + "[T_0->word<h-reg1> * T_0 [EOI], T_0->* word<h-reg1> T_0 [EOI], T_0->EPSILON * [EOI]], "
        + "[ProperName->word<h-reg1> T_0 * [EOI]], "
        + "[T_1->word<h-reg2> * T_1 [EOI], T_1->* word<h-reg2> T_1 [EOI], T_1->EPSILON * [EOI]], "
        + "[ProperName->word<h-reg2> T_1 * [EOI]], "
        + "[T_2->FormOfAddress Person<gnc-agr=[1]> * [EOI]], "
        + "[AdjCoord->AdjCoord<gnc-agr=[1]> 'и' adj<gnc-agr=[1]> * ['человек'], AdjCoord->AdjCoord<gnc-agr=[1]> 'и' adj<gnc-agr=[1]> * ['товарищ'], AdjCoord->AdjCoord<gnc-agr=[1]> 'и' adj<gnc-agr=[1]> * ['мистер'], AdjCoord->AdjCoord<gnc-agr=[1]> 'и' adj<gnc-agr=[1]> * ['или'], AdjCoord->AdjCoord<gnc-agr=[1]> 'и' adj<gnc-agr=[1]> * ['и'], AdjCoord->AdjCoord<gnc-agr=[1]> 'и' adj<gnc-agr=[1]> * ['господин'], AdjCoord->AdjCoord<gnc-agr=[1]> 'и' adj<gnc-agr=[1]> * [word<h-reg2>], AdjCoord->AdjCoord<gnc-agr=[1]> 'и' adj<gnc-agr=[1]> * [word<h-reg1>]], "
        + "[AdjCoord->AdjCoord<gnc-agr=[1]> 'или' adj<gnc-agr=[1]> * ['человек'], AdjCoord->AdjCoord<gnc-agr=[1]> 'или' adj<gnc-agr=[1]> * ['товарищ'], AdjCoord->AdjCoord<gnc-agr=[1]> 'или' adj<gnc-agr=[1]> * ['мистер'], AdjCoord->AdjCoord<gnc-agr=[1]> 'или' adj<gnc-agr=[1]> * ['или'], AdjCoord->AdjCoord<gnc-agr=[1]> 'или' adj<gnc-agr=[1]> * ['и'], AdjCoord->AdjCoord<gnc-agr=[1]> 'или' adj<gnc-agr=[1]> * ['господин'], AdjCoord->AdjCoord<gnc-agr=[1]> 'или' adj<gnc-agr=[1]> * [word<h-reg2>], AdjCoord->AdjCoord<gnc-agr=[1]> 'или' adj<gnc-agr=[1]> * [word<h-reg1>]], "
        + "[S->AdjCoord<gnc-agr=[1]> FormOfAddress Person<gnc-agr=[1]> * [EOI]], "
        + "[T_0->word<h-reg1> T_0 * [EOI]], "
        + "[T_1->word<h-reg2> T_1 * [EOI]]]",
        testable.getStates().toString());

    assertEquals("[{adj=[s1], adj<gnc-agr=[1]>=[s2], AdjCoord<gnc-agr=[1]>=[s3], S=[s4]}, "
        + "{word<h-reg1>=[r(AdjCoord, 1, 0(0))], word<h-reg2>=[r(AdjCoord, 1, 0(0))], 'господин'=[r(AdjCoord, 1, 0(0))], 'и'=[r(AdjCoord, 1, 0(0))], 'или'=[r(AdjCoord, 1, 0(0))], 'мистер'=[r(AdjCoord, 1, 0(0))], 'товарищ'=[r(AdjCoord, 1, 0(0))], 'человек'=[r(AdjCoord, 1, 0(0))]}, "
        + "{adj<gnc-agr=[1]>=[s5], word<h-reg1>=[s6], word<h-reg2>=[s7], 'господин'=[s8], 'мистер'=[s9], 'товарищ'=[s10], 'человек'=[s11], FormOfAddress=[s12], T_2=[s15], Person<gnc-agr=[1]>=[s13], ProperName=[s14]}, "
        + "{word<h-reg1>=[s6], word<h-reg2>=[s7], 'господин'=[s8], 'и'=[s16], 'или'=[s17], 'мистер'=[s9], 'товарищ'=[s10], 'человек'=[s11], FormOfAddress=[s18], Person<gnc-agr=[1]>=[s19], ProperName=[s14]}, "
        + "{EOI=[r(Root, 1, 0(3))/acc]}, "
        + "{adj<gnc-agr=[1]>=[s5], word<h-reg1>=[s6], word<h-reg2>=[s7], 'господин'=[s8], 'мистер'=[s9], 'товарищ'=[s10], 'человек'=[s11], FormOfAddress=[s12], T_2=[s20], Person<gnc-agr=[1]>=[s13], ProperName=[s14]}, "
        + "{T_0=[s22], EOI=[r(ProperName, 1, 1(12)), r(T_0, 0, 1(4))], word<h-reg1>=[s21]}, "
        + "{T_1=[s24], EOI=[r(ProperName, 1, 2(14)), r(T_1, 0, 2(5))], word<h-reg2>=[s23]}, "
        + "{word<h-reg1>=[r(FormOfAddress, 1, 0(10))], word<h-reg2>=[r(FormOfAddress, 1, 0(10))], 'человек'=[r(FormOfAddress, 1, 0(10))]}, "
        + "{word<h-reg1>=[r(FormOfAddress, 1, 0(9))], word<h-reg2>=[r(FormOfAddress, 1, 0(9))], 'человек'=[r(FormOfAddress, 1, 0(9))]}, "
        + "{word<h-reg1>=[r(FormOfAddress, 1, 0(8))], word<h-reg2>=[r(FormOfAddress, 1, 0(8))], 'человек'=[r(FormOfAddress, 1, 0(8))]}, "
        + "{EOI=[r(Person, 1, 0(7))]}, "
        + "{word<h-reg1>=[s6], word<h-reg2>=[s7], 'человек'=[s11], Person<gnc-agr=[1]>=[s25], ProperName=[s14]}, "
        + "{EOI=[r(T_2, 1, 0(18))]}, "
        + "{EOI=[r(Person, 1, 0(6))]}, "
        + "{EOI=[r(S, 2, 0(16))]}, "
        + "{adj<gnc-agr=[1]>=[s26]}, "
        + "{adj<gnc-agr=[1]>=[s27]}, "
        + "{word<h-reg1>=[s6], word<h-reg2>=[s7], 'человек'=[s11], Person<gnc-agr=[1]>=[s28], ProperName=[s14]}, "
        + "{EOI=[r(S, 2, 0(11))]}, "
        + "{EOI=[r(T_2, 2, 0(17))]}, "
        + "{T_0=[s29], EOI=[r(T_0, 1, 1(13)), r(T_0, 0, 1(4))], word<h-reg1>=[s21]}, "
        + "{EOI=[r(ProperName, 2, 0(12))]}, "
        + "{T_1=[s30], EOI=[r(T_1, 1, 2(15)), r(T_1, 0, 2(5))], word<h-reg2>=[s23]}, "
        + "{EOI=[r(ProperName, 2, 0(14))]}, {EOI=[r(T_2, 2, 0(20))]}, "
        + "{word<h-reg1>=[r(AdjCoord, 3, 0(2))], word<h-reg2>=[r(AdjCoord, 3, 0(2))], 'господин'=[r(AdjCoord, 3, 0(2))], 'и'=[r(AdjCoord, 3, 0(2))], 'или'=[r(AdjCoord, 3, 0(2))], 'мистер'=[r(AdjCoord, 3, 0(2))], 'товарищ'=[r(AdjCoord, 3, 0(2))], 'человек'=[r(AdjCoord, 3, 0(2))]}, "
        + "{word<h-reg1>=[r(AdjCoord, 3, 0(1))], word<h-reg2>=[r(AdjCoord, 3, 0(1))], 'господин'=[r(AdjCoord, 3, 0(1))], 'и'=[r(AdjCoord, 3, 0(1))], 'или'=[r(AdjCoord, 3, 0(1))], 'мистер'=[r(AdjCoord, 3, 0(1))], 'товарищ'=[r(AdjCoord, 3, 0(1))], 'человек'=[r(AdjCoord, 3, 0(1))]}, "
        + "{EOI=[r(S, 3, 0(19))]}, "
        + "{EOI=[r(T_0, 2, 0(13))]}, "
        + "{EOI=[r(T_1, 2, 0(15))]}]",
        testable.getRN2ActionTable().toString());

    assertEquals("[0-(adj)->1, "
        + "0-(adj<gnc-agr=[1]>)->2, "
        + "0-(AdjCoord<gnc-agr=[1]>)->3, "
        + "0-(S)->4, "
        + "2-(adj<gnc-agr=[1]>)->5, "
        + "2-(word<h-reg1>)->6, "
        + "2-(word<h-reg2>)->7, "
        + "2-('господин')->8, "
        + "2-('мистер')->9, "
        + "2-('товарищ')->10, "
        + "2-('человек')->11, "
        + "2-(FormOfAddress)->12, "
        + "2-(Person<gnc-agr=[1]>)->13, "
        + "2-(ProperName)->14, "
        + "2-(T_2)->15, "
        + "3-(word<h-reg1>)->6, "
        + "3-(word<h-reg2>)->7, "
        + "3-('господин')->8, "
        + "3-('и')->16, "
        + "3-('или')->17, "
        + "3-('мистер')->9, "
        + "3-('товарищ')->10, "
        + "3-('человек')->11, "
        + "3-(FormOfAddress)->18, "
        + "3-(Person<gnc-agr=[1]>)->19, "
        + "3-(ProperName)->14, "
        + "5-(adj<gnc-agr=[1]>)->5, "
        + "5-(word<h-reg1>)->6, "
        + "5-(word<h-reg2>)->7, "
        + "5-('господин')->8, "
        + "5-('мистер')->9, "
        + "5-('товарищ')->10, "
        + "5-('человек')->11, "
        + "5-(FormOfAddress)->12, "
        + "5-(Person<gnc-agr=[1]>)->13, "
        + "5-(ProperName)->14, "
        + "5-(T_2)->20, "
        + "6-(word<h-reg1>)->21, "
        + "6-(T_0)->22, "
        + "7-(word<h-reg2>)->23, "
        + "7-(T_1)->24, "
        + "12-(word<h-reg1>)->6, "
        + "12-(word<h-reg2>)->7, "
        + "12-('человек')->11, "
        + "12-(Person<gnc-agr=[1]>)->25, "
        + "12-(ProperName)->14, "
        + "16-(adj<gnc-agr=[1]>)->26, "
        + "17-(adj<gnc-agr=[1]>)->27, "
        + "18-(word<h-reg1>)->6, "
        + "18-(word<h-reg2>)->7, "
        + "18-('человек')->11, "
        + "18-(Person<gnc-agr=[1]>)->28, "
        + "18-(ProperName)->14, "
        + "21-(word<h-reg1>)->21, "
        + "21-(T_0)->29, "
        + "23-(word<h-reg2>)->23, "
        + "23-(T_1)->30]",
        testable.getStateTransitions().toString());
    }
  
}
