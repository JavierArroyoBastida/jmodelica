package RedeclareTests

model RedeclareTestOx1 "Basic redeclare test"
 annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.FlatteningTestCase(name="RedeclareTestOx1",
                                               description="Basic redeclares.",
                                               flatModel=
"fclass RedeclareTests.RedeclareTestOx1
 Real c.a.x = 2 /*(2)*/;
 Real c.a.y = 3 /*(3)*/;
equation
end RedeclareTests.RedeclareTestOx1;
")})));
 
 // This is perfectly ok.
 
  model A
    Real x=1;
  end A;
 
  model B
   Real x=2;
   Real y=3;
  end B;
 
   model C
     replaceable A a;
   end C;
 
   C c(redeclare B a);
 
end RedeclareTestOx1;
 
model RedeclareTestOx2_Err "Basic redeclare test, errounous"
 
 
      annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.ErrorTestCase(name="RedeclareTestOx2_Err",
        description="Test basic redeclares. Error caused by failed subtype test in component redeclaration.",
                                               errorMessage=
"
1 error(s) found...
In file 'src/test/modelica/RedeclareTests.mo':
Semantic error at line 71, column 8:
  'redeclare A b' is not a subtype of 'replaceable B b'

  "
  )})));
 
/*
  Should give an error message like
  Error in redeclaration in component:
    C c(redeclare A b)
   component 'A b' is not a subtype of component 'B a'.
   
   
   
 
*/
 
  model A
    Real x=1;
  end A;
 
  model B
   Real x=2;
   Real y=3;
  end B;
 
   model C
     replaceable B b;
   end C;
   // Here is the error
   C c(redeclare A b);
 
end RedeclareTestOx2_Err;
 
model RedeclareTestOx3 "Redeclare deeper into instance hierarchy."

  annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.FlatteningTestCase(name="RedeclareTestOx3",
        description="Basic test of redeclares.",
                                               flatModel=
"fclass RedeclareTests.RedeclareTestOx3
 Real d.c.a.x = 2 /*(2)*/;
 Real d.c.a.y = 3 /*(3)*/;
equation 
end RedeclareTests.RedeclareTestOx3;
")})));


 
  // Perfectly ok.
 
  model A
    Real x=1;
  end A;
 
  model B
   Real x=2;
   Real y=3;
  end B;
 
   model C
     replaceable A a;
   end C;
 
   model D
     C c;
   end D;
 
   D d(c(redeclare B a));
 
end RedeclareTestOx3;
 
model RedeclareTestOx4_Err 
    "Redeclare deeper into instance hierarchy."
 
  
      annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.ErrorTestCase(name="RedeclareTestOx4_Err",
        description="Test basic redeclares. Error caused by failed subtype test in component redeclaration.",
                                               errorMessage=
"
1 error(s) found...
In file 'src/test/modelica/RedeclareTests.mo':
Semantic error at line 159, column 10:
  'redeclare A b' is not a subtype of 'replaceable B b'

  "
  )})));
 
/*
  Should give an error message like
  Error in redeclaration in component:
    D d(c(redeclare A b)) in class RedeclareTestOx4_Err
   component 'A b' is not a subtype of component 'B b'.
   Original declaration located in class C. 
   Instance name of original declaration: d.c.b   
 
 
 
*/
 
  model A
    Real x=1;
  end A;
 
  model B
   Real x=2;
   Real y=3;
  end B;
 
   model C
     replaceable B b;
   end C;
 
   model D
     C c;
   end D;
 
   D d(c(redeclare A b));
 
end RedeclareTestOx4_Err;
 
model RedeclareTestOx5 
    "Redeclare deeper into instance hierarchy and redeclaration of a replacing component."
 
  annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.FlatteningTestCase(name="RedeclareTestOx5",
        description="Basic test of redeclares.",
                                               flatModel=
"fclass RedeclareTests.RedeclareTestOx5
 Real e.d.a.x = 2 /*(2)*/;
 Real e.d.a.y = 3 /*(3)*/;
 Real e.d.a.z = 4 /*(4)*/;
equation 
end RedeclareTests.RedeclareTestOx5;
")})));
 
 
  model A
    Real x=1;
  end A;
 
  model B
   Real x=2;
   Real y=3;
  end B;
 
  model C
   Real x=2;
   Real y=3;
   Real z=4;
  end C;
 
   model D
     replaceable A a;
   end D;
 
   model E
     D d(redeclare B a);
   end E;
 
   E e(d(redeclare C a));
 
end RedeclareTestOx5;
 
model RedeclareTestOx6_Err 
    "Redeclare deeper into instance hierarchy and redeclaration of a replacing component, Errouneous?"
 
  
/*
  This test case test tests lookup in a redeclared component and is currently
  not supported.   
 
*/
 
  model A
    Real x=1;
  end A;
 
  model B
   Real x=2;
   Real y=3;
  end B;
 
  model C
   Real x=2;
   Real y=3;
   Real z=4;
  end C;
 
   model D
     replaceable A a;
   end D;
 
   model E
     D d(redeclare replaceable C a);
     Real q = d.a.z; // This should not be ok since the constraining class of component a is A.
   end E;
 
   E e(d(redeclare B a)); // This redeclaration should be ok since B is a subtype of A!
 
end RedeclareTestOx6_Err;
 
 model RedeclareTestOx65_Err 
    "Redeclare deeper into instance hierarchy and redeclaration of a replacing component, Errouneous?"
 
 
      annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.ErrorTestCase(name="RedeclareTestOx65_Err",
        description="Test basic redeclares. Error caused by failed subtype test in component redeclaration.",
                                               errorMessage=
"
1 error(s) found...
In file 'src/test/modelica/RedeclareTests.mo':
Semantic error at line 276, column 10:
  'redeclare replaceable A a' is not a subtype of 'replaceable B a'

  "
  )})));
 
/*
  Should give an error message like
   
 
*/
 
  model A
    Real x=1;
  end A;
 
  model B
   Real x=2;
   Real y=3;
  end B;
 
  model C
   Real x=2;
   Real y=3;
   Real z=4;
  end C;
 
   model D
     replaceable B a;
   end D;
 
   model E
     D d(redeclare replaceable A a);
   end E;
 
   E e(d(redeclare C a)); 
 
end RedeclareTestOx65_Err;
 
 
model RedeclareTestOx7_Err 
    "Redeclare deeper into instance hierarchy and redeclaration of a replacing component, Errouneous?"
  // This is based on replaceable types and is not tested here.
 
  model A
    Real x=1;
  end A;
 
  model B
   Real x=2;
   Real y=3;
  end B;
 
  model C
   Real x=2;
   Real y=3;
   Real z=4;
  end C;
 
   model D
     replaceable model AA = A;
     AA a;
   end D;
 
   model E
     D d(redeclare replaceable model AA=C);
     Real q = d.a.z; // This should not be ok!
   end E;
 
   E e(d(redeclare model AA=B)); // This redeclaration should be ok since B is a subtype of A!
 
end RedeclareTestOx7_Err;
 
model RedeclareTestOx8 "Constraining clause example"
 
   annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.FlatteningTestCase(name="RedeclareTestOx8",
        description="Basic test of redeclares.",
                                               flatModel=
"fclass RedeclareTests.RedeclareTestOx8
 Real d.c.x = 2 /*(2)*/;
 Real d.c.y = 3 /*(3)*/;
equation 
end RedeclareTests.RedeclareTestOx8;
")})));
 
 
  model A
    Real x=1;
  end A;
 
  model B
   Real x=2;
   Real y=3;
  end B;
 
  model C
   Real x=2;
   Real y=3;
   Real z=4;
  end C;
 
   model D
     replaceable C c extends B;
   end D;
   // Ok, since the constraining clause of C c is B.
   D d(redeclare B c);
 
end RedeclareTestOx8;
 
model RedeclareTestOx9_Err "Constraining clause example, errouneous"
 
 
      annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.ErrorTestCase(name="RedeclareTestOx9_Err",
        description="Test basic redeclares. Error caused by failed subtype test in component redeclaration.",
                                               errorMessage=
"1 error(s) found...
In file 'src/test/modelica/RedeclareTests.mo':
Semantic error at line 399, column 8:
  'redeclare A c' is not a subtype of 'replaceable C c extends B '

  "
  )})));
 
 
 
 
 /*
  Should give an error message like
  Error in redeclaration in component:
   D d(redeclare A c); in class RedeclareTestOx9_Err
   component 'A c' is not a subtype of constraining type B.
   Redeclared declaration located in class D.
   replaceable C c extends B;
   Instance name of redeclared original declaration: d.c   
   TODO: the check is correct, but the error message is not correct.
*/
 
  model A
    Real x=1;
  end A;
 
  model B
   Real x=2;
   Real y=3;
  end B;
 
  model C
   Real x=2;
   Real y=3;
   Real z=4;
  end C;
 
   model D
     replaceable C c extends B;
   end D;
 
   D d(redeclare A c);
 
end RedeclareTestOx9_Err;

model RedeclareTestOx95_Err "Constraining clause example, errouneous"
 
      annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.ErrorTestCase(name="RedeclareTestOx95_Err",
        description="Check that the declaration is a subtype of the constraining clause",
                                               errorMessage=
"1 error(s) found...
In file 'src/test/modelica/RedeclareTests.mo':
Semantic error at line 448, column 10:
  'B' is not a subtype of 'C'
  "
  )})));
 
 
 
 
  model A
    Real x=1;
  end A;
 
  model B
   Real x=2;
   Real y=3;
  end B;
 
  model C
   Real x=2;
   Real y=3;
   Real z=4;
  end C;
 
   model D
     replaceable B b extends C;
   end D;
 
   D d;
 
end RedeclareTestOx95_Err;
 
model RedeclareTestOx10 "Constraining clause example."
 /* This is test case does not work currently!*/
    annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.FlatteningTestCase(name="RedeclareTestOx10",
        description="Basic test of redeclares.",
                                               flatModel=
"fclass RedeclareTests.RedeclareTestOx8

end RedeclareTests.RedeclareTestOx8;
")})));
 
 
 
  model A
    Real x=1;
  end A;
 
  model B
   Real x=2;
   Real y=3;
  end B;
 
  model C
   Real x=2;
   Real y=3;
   Real z=4;
  end C;
 
   model D
     replaceable B c extends A;
   end D;
 
   model E
     // This is actually ok since the replacing component does not have an
     // explicit constraining clause, in which case the constraining clause
     // of the original declaration is used.
     replaceable D d extends D(redeclare replaceable B c);
   end E;
 
   E e(redeclare D d(redeclare A c));
 
end RedeclareTestOx10;
 
model RedeclareTestOx11_Err "Constraining clause example."
      annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.ErrorTestCase(name="RedeclareTestOx11_Err",
        description="Check that the declaration is a subtype of the constraining clause",
                                               errorMessage=
"
  "
  )})));
/*
  Should give an error message like
  Error in redeclaration in component:
   D d(redeclare A c); in class RedeclareTestOx9_Err
   component 'A c' is not a subtype of constraining type B.
   Redeclared declaration located in class D.
   replaceable C c extends B;
   Instance name of redeclared original declaration: d.c   
*/
 
  model A
    Real x=1;
  end A;
 
  model B
   Real x=2;
   Real y=3;
  end B;
 
  model C
   Real x=2;
   Real y=3;
   Real z=4;
  end C;
 
   model D
     replaceable B c;
   end D;
 
   model E
     /* Component decl triggers tests:
          1. The original decl:
            1.2 Type check of the constraining clause
                  'D(redeclare replaceable B c extends A)'
                in the environment 
                  {redeclare A c}
                correponding to myEnvironment("d")
                
                ** Result **
                 */
     replaceable D d extends D(redeclare replaceable B c extends A);
   end E;
   
   // This should be ok.
   // This declaration does not trigger any tests
   E e(redeclare D d(redeclare A c));
 
end RedeclareTestOx11_Err;
 
 model RedeclareTestOx115_Err "Constraining clause example."
      annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.ErrorTestCase(name="RedeclareTestOx115_Err",
        description="Check that the declaration is a subtype of the constraining clause",
                                               errorMessage=
"1 error(s) found...
In file 'src/test/modelica/RedeclareTests.mo':
Semantic error at line 594, column 32:
  'B' is not a subtype of 'C'
  "
  )})));
/*
  Should give an error message like
  In file 'src/test/modelica/RedeclareTests.mo':
Semantic error at line 431, column 32:
  'B' is not a subtype of 'C'
*/
 
  model A
    Real x=1;
  end A;
 
  model B
   Real x=2;
   Real y=3;
  end B;
 
  model C
   Real x=2;
   Real y=3;
   Real z=4;
  end C;
 
   model D
     replaceable B c;
   end D;
 
   model E
     replaceable D d extends D(redeclare replaceable B c extends C);
   end E;
  
 
   E e;
 
end RedeclareTestOx115_Err;

 model RedeclareTestOx116_Err "Constraining clause example."
      annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.ErrorTestCase(name="RedeclareTestOx116_Err",
        description="Check that the declaration is a subtype of the constraining clause",
                                               errorMessage=
"1 error(s) found...
In file 'src/test/modelica/RedeclareTests.mo':
Semantic error at line 644, column 58:
  'A' is not a subtype of 'B'
  "
  )})));
/*
  Should give an error message like
  In file 'src\test\modelica\RedeclareTests.mo':
Semantic error at line 470, column 58:
  'A' is not a subtype of 'B'
  
 
 
 */
 
  model A
    Real x=1;
  end A;
 
  model B
   Real x=2;
   Real y=3;
  end B;
 
  model C
   Real x=2;
   Real y=3;
   Real z=4;
  end C;
 
   model D
     replaceable B c;
   end D;
 
   model E 
     // This is an error because the constraining clause of C c extends A is not a subtype of B c
     replaceable D d extends D(redeclare replaceable C c extends A);
   end E;
  
 
   E e;
 
end RedeclareTestOx116_Err;
 
model RedeclareTestOx12 "Constraining clause example."
 
  model A
    Real x=1;
  end A;
 
  model B
   Real x=2;
   Real y=3;
  end B;
 
   model D
     //Here the modifiers (x=3,y=3) are not used when the component is redeclared.
     replaceable B c(x=3,y=3) extends A(x=5);
   end D;
 
   D d(redeclare A c);
 
end RedeclareTestOx12;
 
model RedeclareTestOx13 "Constraining clause example."
 
  model A
    Real x=1;
  end A;
 
  model B
   Real x=2;
   Real y=3;
  end B;
 
  model C
   Real x=2;
   Real y=3;
   Real z=4;
  end C;
 
   model D
     replaceable A c;
   end D;
 
   model E
     D d( redeclare replaceable B c(y=10) extends A);
   end E;
 
   E e(d(redeclare C c(z=3)));
 
end RedeclareTestOx13;
 
model RedeclareTestOx6b_Err
 model A
    Real x=1;
 end A;
 
  model B
   Real x=2;
   Real y=3;
  end B;
 
   model D
     replaceable A a;
   end D;
 
   model E
     D d(redeclare replaceable B a extends B);
     Real q = d.a.y; // This access should not be ok since y is not part of
                     // the constraining interface of the replacing
                     // component B b. The constraining interface of the
                     // replacing component B b is rather defined by the
                     // original declaration A a.
   end E;
 
   // This is a perfectly legal redeclare, since the constraining class of 
   // the replacing declaration B a is A a
   E e(d(redeclare A a));
 
end RedeclareTestOx6b_Err;


model RedeclareTest0
  model A
    Real x;
  end A;
  
  model B
   Real x;
   Real y;
  end B;
  
  model B2
   Real x;
   Real y;
   Real z;
  end B2;

  
   model C
     replaceable A a;
   end C;
   
   model D
     C c(redeclare B a);
   end D;
   
   D d(c(redeclare B2 a));
   
 

end RedeclareTest0;



	model RedeclareTest1
  
     annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.FlatteningTestCase(name="RedeclareTest1",
                                               description="Basic redeclares.",
                                               flatModel=
"
 fclass RedeclareTests.RedeclareTest1
 Real a.c2.x = 3 /*(3)*/;
 Real a.c2.y = 4 /*(4)*/;
 Real b.a.c2.x = 8 /*(8)*/;
 Real b.a.c2.y = 4 /*(4)*/;
 Real b.a.c2.z = 9 /*(9)*/;
 Real b.aa.c2.x = 2 /*(2)*/;
equation
end RedeclareTests.RedeclareTest1;
")})));

  
  model C2
    Real x=2;
  end C2;
  
  model C22
    Real x=3;
    Real y=4;
  end C22;
  
  model C222
    Real x=5;
    Real y=6;
    Real z=7;
  end C222;
  
  model A
    replaceable C2 c2;
  end A;
  
  model B
    // Try to access a parameter here as well
    A a(redeclare replaceable C22 c2(x=8));
    A aa;
  end B;
  
  A a(redeclare C22 c2);
  B b(a(redeclare C222 c2(z=9,y=4)));
end RedeclareTest1;


model RedeclareTest2
  
       annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.FlatteningTestCase(name="RedeclareTest2",
                                               description="Basic redeclares.",
                                               flatModel=
"
/*
fclass RedeclareTests.RedeclareTest2;
  Real p.c1.r = 1;
  Real p.c1.g = 0.1;
  Real p.c1.b = 0.1;
  Real p.c2.r = 0;
  Real p.c2.g = 0;
  Real p.c2.b = 1;
  Real p.c3.r = 0;
  Real p.c3.g = 1;
  Real p.c3.b = 0;
equation 
end RedeclareTests.RedeclareTest2;
*/
fclass RedeclareTests.RedeclareTest2
 Real p.c1.r = 1 /*(1)*/;
 Real p.c1.g = 0.1 /*(0.1)*/;
 Real p.c1.b = 0.1 /*(0.1)*/;
 Real p.c2.r = 0 /*(0)*/;
 Real p.c2.g = 0 /*(0)*/;
 Real p.c2.b = 1 /*(1)*/;
 Real p.c3.r = 0 /*(0)*/;
 Real p.c3.g = 1 /*(1)*/;
 Real p.c3.b = 0 /*(0)*/;
equation
end RedeclareTests.RedeclareTest2;
")})));
  
  
  model Color 
    Real r=0;
    Real g=0;
    Real b=0;
  end Color;
  
  model Red 
    extends Color(r=1);
  end Red;
  
  model Green 
    extends Color(g=1);
  end Green;
  
  model Blue 
    extends Color(b=1);
  end Blue;
  
  model Palette 
     replaceable Color c1;
     replaceable Color c2;
     replaceable Color c3;
  end Palette;
  
  Palette p(redeclare Red c1(r=1,g=0.1,b=0.1),redeclare Blue c2,redeclare Green c3);
  
end RedeclareTest2;

model RedeclareTest3 
  
        annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.FlatteningTestCase(name="RedeclareTest3",
                                               description="Basic redeclares.",
                                               flatModel=
"
/*
fclass RedeclareTests.RedeclareTest3;
  Real r = 0.1;
  Real palette.q = 0.3;
  Real palette.p.b = 0.001;
  Real palette.p.c1.rr = 0;
  Real palette.p.c1.r = 1;
  Real palette.p.c1.g = 0.2;
  Real palette.p.c1.b = palette.q;
  Real palette.p.c2.rr = 0;
  Real palette.p.c2.r = palette.p.c2.rr;
  Real palette.p.c2.g = 0;
  Real palette.p.c2.b = 1;
  Real palette.p.c3.gg = 1;
  Real palette.p.c3.rr = 0;
  Real palette.p.c3.r = r;
  Real palette.p.c3.g = palette.p.c3.gg;
  Real palette.p.c3.b = 0.002;
  Real palette.p.q = 0.4;
  Real q = 0.3;
  Real p.b = 0.001;
  Real p.c1.rr = 0;
  Real p.c1.r = 1;
  Real p.c1.g = 0.1;
  Real p.c1.b = 0.23;
  Real p.c2.rr = 0;
  Real p.c2.r = p.c2.rr;
  Real p.c2.g = 0;
  Real p.c2.b = 1;
  Real p.c3.gg = 1;
  Real p.c3.rr = 0;
  Real p.c3.r = 0.56;
  Real p.c3.g = 0.85;
  Real p.c3.b = 0.24;
  Real p.q = 0.4;
equation 
end RedeclareTests.RedeclareTest3;
*/
fclass RedeclareTests.RedeclareTest3
 Real r = 0.1 /*(0.1)*/;
 Real palette.q = 0.3 /*(0.3)*/;
 Real palette.p.b = 0.001 /*(0.0010)*/;
 Real palette.p.c1.rr = 0 /*(0)*/;
 Real palette.p.c1.r = 1 /*(1)*/;
 Real palette.p.c1.g = 0.2 /*(0.2)*/;
 Real palette.p.c1.b = palette.q;
 Real palette.p.c2.rr = 0 /*(0)*/;
 Real palette.p.c2.r = palette.p.c2.rr;
 Real palette.p.c2.g = 0 /*(0)*/;
 Real palette.p.c2.b = 1 /*(1)*/;
 Real palette.p.c3.gg = 1 /*(1)*/;
 Real palette.p.c3.rr = 0 /*(0)*/;
 Real palette.p.c3.r = r;
 Real palette.p.c3.g = palette.p.c3.gg;
 Real palette.p.c3.b = 0.002 /*(0.0020)*/;
 Real palette.p.q = 0.4 /*(0.4)*/;
 Real q = 0.3 /*(0.3)*/;
 Real p.b = 0.001 /*(0.0010)*/;
 Real p.c1.rr = 0 /*(0)*/;
 Real p.c1.r = 1 /*(1)*/;
 Real p.c1.g = 0.1 /*(0.1)*/;
 Real p.c1.b = 0.23 /*(0.23)*/;
 Real p.c2.rr = 0 /*(0)*/;
 Real p.c2.r = p.c2.rr;
 Real p.c2.g = 0 /*(0)*/;
 Real p.c2.b = 1 /*(1)*/;
 Real p.c3.gg = 1 /*(1)*/;
 Real p.c3.rr = 0 /*(0)*/;
 Real p.c3.r = 0.56 /*(0.56)*/;
 Real p.c3.g = 0.85 /*(0.85)*/;
 Real p.c3.b = 0.24 /*(0.24)*/;
 Real p.q = 0.4 /*(0.4)*/;
equation
end RedeclareTests.RedeclareTest3;
")})));
  
  
  
  extends C0.Colors.MyPalette(p(redeclare C0.Colors.Green c3(r=0.56,g=0.85,b=0.24),c1(b=0.23)));
model C0 
    
  model Colors 
  model Color 
    Real rr = 0;
    Real r=rr;
    Real g=0;
    Real b=0;
  end Color;
      
  model Red 
    extends Color(r=1);
  end Red;
      
  model Green
    extends Color(g=gg);
    Real gg = 1; 
  end Green;
      
  model Blue 
    extends Color(b=1);
  end Blue;
      
  model Palette 
     Real b = 0.001;
     replaceable Color c1;
     replaceable Color c2;
     replaceable Color c3(b=b);
     Real q = 0.4;
  end Palette;
      
  model MyPalette 
        
  Real q = 0.3;
        
  Palette p(redeclare replaceable Red c1(g=0.1,b=q),redeclare replaceable Blue c2,
            redeclare replaceable Green c3(b=0.002));
        
  end MyPalette;
      
  end Colors;
    
end C0;
  
  Real r = 0.1;
 C0.Colors.MyPalette palette(p(c3(r=r),redeclare C0.Colors.Red c1(g=0.2)));
  
end RedeclareTest3;


model RedeclareTest4
  
  
         annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.FlatteningTestCase(name="RedeclareTest4",
                                               description="Basic redeclares.",
                                               flatModel=
"
/*
fclass Cm1
  Real c0_q = 0.3;
  Real c0_p_c1_r = 1.0;
  Real c0_p_c1_g = 0.2;
  Real c0_p_c1_b = c0_q;
  Real c0_p_c2_r = 0.0;
  Real c0_p_c2_g = 0.0;
  Real c0_p_c2_b = 1.0;
  Real c0_p_c3_r = 0.1;
  Real c0_p_c3_g = 1.0;
  Real c0_p_c3_b = 0.0010;
  Real q = 0.3;
  Real p_c1_r = 1.0;
  Real p_c1_g = 0.1;
  Real p_c1_b = 0.23;
  Real p_c2_r = 0.0;
  Real p_c2_g = 0.0;
  Real p_c2_b = 1.0;
  Real p_c3_r = 0.56;
  Real p_c3_g = 0.85;
  Real p_c3_b = 0.24;
equation 
end Cm1;
*/
fclass RedeclareTests.RedeclareTest4
 Real c0.q = 0.3 /*(0.3)*/;
 Real c0.p.c1.r = 1 /*(1)*/;
 Real c0.p.c1.g = 0.2 /*(0.2)*/;
 Real c0.p.c1.b = c0.q;
 Real c0.p.c2.r = 0 /*(0)*/;
 Real c0.p.c2.g = 0 /*(0)*/;
 Real c0.p.c2.b = 1 /*(1)*/;
 Real c0.p.c3.r = 0.1 /*(0.1)*/;
 Real c0.p.c3.g = 1 /*(1)*/;
 Real c0.p.c3.b = 0.001 /*(0.0010)*/;
 Real q = 0.3 /*(0.3)*/;
 Real p.c1.r = 1 /*(1)*/;
 Real p.c1.g = 0.1 /*(0.1)*/;
 Real p.c1.b = 0.23 /*(0.23)*/;
 Real p.c2.r = 0 /*(0)*/;
 Real p.c2.g = 0 /*(0)*/;
 Real p.c2.b = 1 /*(1)*/;
 Real p.c3.r = 0.56 /*(0.56)*/;
 Real p.c3.g = 0.85 /*(0.85)*/;
 Real p.c3.b = 0.24 /*(0.24)*/;
equation
end RedeclareTests.RedeclareTest4;
")})));
  
 
  extends C0(p(redeclare C0.Green c3(r=0.56,g=0.85,b=0.24),c1(b=0.23)));
model C0 
  
  
  model Color 
    Real r=0;
    Real g=0;
    Real b=0;
  end Color;
  
  model Red 
    extends Color(r=1);
  end Red;
  
  model Green 
    extends Color(g=1);
  end Green;
  
  model Blue 
    extends Color(b=1);
  end Blue;
  
  model Palette 
     replaceable Color c1;
     replaceable Color c2;
     replaceable Color c3(b=0.001);
  end Palette;
  
  Real q = 0.3;
  
  Palette p(redeclare replaceable Red c1(g=0.1,b=q),redeclare replaceable Blue c2,redeclare replaceable Green c3);
  
end C0;

 C0 c0(p(c3(r=0.1),redeclare C0.Red c1(g=0.2)));

  

end RedeclareTest4;


model RedeclareTest5
  
  
    annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.FlatteningTestCase(name="RedeclareTest5",
                                               description="Basic redeclares.",
                                               flatModel=
"
fclass RedeclareTests.RedeclareTest5
 Real u.c1.c2.x = 55 /*(55)*/;
 Real u.c1.c2.y = 44 /*(44)*/;
 Real u.c1.c2.z = 66 /*(66)*/;
equation
end RedeclareTests.RedeclareTest5;
")})));



model Unnamed 
  model C1 
    model C2 
      Real x=2;
    end C2;
    replaceable C2 c2(x=4);
  end C1;
    
  model C22 
     Real x=1;
     Real y=3;
  end C22;
    
  model C222 
    Real x=11;
     Real y=33;
  end C222;
    
  C1 c1(redeclare replaceable C222 c2(y=44),c2.x=2);
    
end Unnamed;
  
model C222
     Real x=2;
     Real y=4;
     Real z=6;
end C222;
 Unnamed u(c1(redeclare C222 c2(x=55,z=66)));

end RedeclareTest5;

model RedeclareTest6
      annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.FlatteningTestCase(name="RedeclareTest6",
                                               description="Basic redeclares.",
                                               flatModel=
"
fclass RedeclareTests.RedeclareTest6
 Real b.a.c2.x = 6 /*(6)*/;
 Real b.a.c2.y = 4 /*(4)*/;
 Real b.a.c2.z = 9 /*(9)*/;
 Real bb.a.c2.x = 55 /*(55)*/;
 Real bb.a.c2.y = 8 /*(8)*/;
equation
end RedeclareTests.RedeclareTest6;
")})));
  model C2
    Real x=2;
  end C2;
  
  model C22
    Real x=3;
    Real y=4;
  end C22;
  
  model C222
    Real x=5;
    Real y=6;
    Real z=7;
  end C222;
  
  model A
    replaceable C2 c2(x=55); 
  end A;
  
  model B
    A a(redeclare replaceable C22 c2(y=8));
  end B;
  
  B b(a(redeclare C222 c2(z=9,y=4,x=6)));
  B bb;

end RedeclareTest6;

model RedeclareTest7
 annotation(JModelica(unitTesting = JModelica.UnitTesting(testCase={
      JModelica.UnitTesting.FlatteningTestCase(name="RedeclareTest7",
                                               description="Basic redeclares.",
                                               flatModel=
"
fclass RedeclareTests.RedeclareTest7
 Real p = 4 /*(4)*/;
 Real a.d.b.x = p;
 Real a.d.b.y = 5 /*(5)*/;
equation
end RedeclareTests.RedeclareTest7;
")})));


  model A
  	model B
    	Real x=3;
 	 	end B;
  	model C
    	Real x=4;
    	Real y=5;
  	end C;
  	model D
  	  replaceable B b(x=0);
  	end D;
  	D d(b(x=7));
  end A;
  Real p=4;
  A a(d(redeclare A.C b(x=p)));
end RedeclareTest7;

model RedeclareTest8
  
  model C2
    Real x=2;
    
    
  end C2;
  
  model C22
    Real x=3;
    Real y=4;
    Real w=5;
  end C22;
  
  model C222
    Real x=5;
    Real y=6;
		Real w=4;
    Real z=7;
  end C222;
  
  model A
    Real q = 1;
    replaceable C2 c2;
  end A;
  
  model B
    Real p =1;
    A a(redeclare replaceable C22 c2(x=8,y=10),q=2);
    A a1(redeclare replaceable C22 c2(x=p));
    A aa;
  end B;
  
  A a(redeclare C22 c2);
  B b(a(redeclare C222 c2(z=9,y=4)));
end RedeclareTest8;


end RedeclareTests;