import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;

class FieldAccessTest {

    class A {
        int a;
    }

    class B extends A {
        int b;
    }

    class C extends B {
        int c;
    }

    int miAa(@Initialized A x) {
        return x.a;
    }

    int mkAa(@UnknownInitialization A x) {
        // :: error: (initialization.invalid.field.access)
        return x.a;
    }

    int mkAAa(@UnknownInitialization(A.class) A x) {
        return x.a;
    }

    int mkBAa(@UnknownInitialization(B.class) A x) {
        // :: error: (initialization.invalid.field.access)
        return x.a;
    }

    int mkCAa(@UnknownInitialization(C.class) A x) {
        // :: error: (initialization.invalid.field.access)
        return x.a;
    }

    int mdAa(@UnderInitialization A x) {
        // :: error: (initialization.invalid.field.access)
        return x.a;
    }

    int mdAAa(@UnderInitialization(A.class) A x) {
        return x.a;
    }

    int mdBAa(@UnderInitialization(B.class) A x) {
        // :: error: (initialization.invalid.field.access)
        return x.a;
    }

    int mdCAa(@UnderInitialization(C.class) A x) {
        // :: error: (initialization.invalid.field.access)
        return x.a;
    }

    int miAa(@Initialized B x) {
        return x.a;
    }

    int mkBa(@UnknownInitialization B x) {
        // :: error: (initialization.invalid.field.access)
        return x.a;
    }

    int mkABa(@UnknownInitialization(A.class) B x) {
        return x.a;
    }

    int mkBBa(@UnknownInitialization(B.class) B x) {
        // :: error: (initialization.invalid.field.access)
        return x.a;
    }

    int mkCBa(@UnknownInitialization(C.class) B x) {
        // :: error: (initialization.invalid.field.access)
        return x.a;
    }

    int mdBa(@UnderInitialization B x) {
        // :: error: (initialization.invalid.field.access)
        return x.a;
    }

    int mdABa(@UnderInitialization(A.class) B x) {
        return x.a;
    }

    int mdBBa(@UnderInitialization(B.class) B x) {
        // :: error: (initialization.invalid.field.access)
        return x.a;
    }

    int mdCBa(@UnderInitialization(C.class) B x) {
        // :: error: (initialization.invalid.field.access)
        return x.a;
    }

    int mkBb(@UnknownInitialization B x) {
        // :: error: (initialization.invalid.field.access)
        return x.b;
    }

    int mkABb(@UnknownInitialization(A.class) B x) {
        return x.b;
    }

    int mkBBb(@UnknownInitialization(B.class) B x) {
        return x.b;
    }

    int mkCBb(@UnknownInitialization(C.class) B x) {
        // :: error: (initialization.invalid.field.access)
        return x.b;
    }

    int mdBb(@UnderInitialization B x) {
        // :: error: (initialization.invalid.field.access)
        return x.b;
    }

    int mdABb(@UnderInitialization(A.class) B x) {
        return x.b;
    }

    int mdBBb(@UnderInitialization(B.class) B x) {
        return x.b;
    }

    int mdCBb(@UnderInitialization(C.class) B x) {
        // :: error: (initialization.invalid.field.access)
        return x.b;
    }

    int miAa(@Initialized C x) {
        return x.a;
    }

    int mkCa(@UnknownInitialization C x) {
        // :: error: (initialization.invalid.field.access)
        return x.a;
    }

    int mkACa(@UnknownInitialization(A.class) C x) {
        return x.a;
    }

    int mkBCa(@UnknownInitialization(B.class) C x) {
        return x.a;
    }

    int mkCCa(@UnknownInitialization(C.class) C x) {
        return x.a;
    }

    int mdCa(@UnderInitialization C x) {
        // :: error: (initialization.invalid.field.access)
        return x.a;
    }

    int mdACa(@UnderInitialization(A.class) C x) {
        return x.a;
    }

    int mdBCa(@UnderInitialization(B.class) C x) {
        return x.a;
    }

    int mdCCa(@UnderInitialization(C.class) C x) {
        return x.a;
    }

    int mkCb(@UnknownInitialization C x) {
        // :: error: (initialization.invalid.field.access)
        return x.b;
    }

    int mkACb(@UnknownInitialization(A.class) C x) {
        return x.b;
    }

    int mkBCb(@UnknownInitialization(B.class) C x) {
        return x.b;
    }

    int mkCCb(@UnknownInitialization(C.class) C x) {
        // :: error: (initialization.invalid.field.access)
        return x.b;
    }

    int mdCb(@UnderInitialization C x) {
        // :: error: (initialization.invalid.field.access)
        return x.b;
    }

    int mdACb(@UnderInitialization(A.class) C x) {
        // :: error: (initialization.invalid.field.access)
        return x.b;
    }

    int mdBCb(@UnderInitialization(B.class) C x) {
        return x.b;
    }

    int mdCCb(@UnderInitialization(C.class) C x) {
        return x.b;
    }

    int mkCc(@UnknownInitialization C x) {
        // :: error: (initialization.invalid.field.access)
        return x.c;
    }

    int mkACc(@UnknownInitialization(A.class) C x) {
        // :: error: (initialization.invalid.field.access)
        return x.c;
    }

    int mkBCc(@UnknownInitialization(B.class) C x) {
        // :: error: (initialization.invalid.field.access)
        return x.c;
    }

    int mkCCc(@UnknownInitialization(C.class) C x) {
        return x.c;
    }

    int mdCc(@UnderInitialization C x) {
        // :: error: (initialization.invalid.field.access)
        return x.c;
    }

    int mdACc(@UnderInitialization(A.class) C x) {
        // :: error: (initialization.invalid.field.access)
        return x.c;
    }

    int mdBCc(@UnderInitialization(B.class) C x) {
        // :: error: (initialization.invalid.field.access)
        return x.c;
    }

    int mdCCc(@UnderInitialization(C.class) C x) {
        return x.c;
    }
}
