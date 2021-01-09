package org.checkerframework.javacutil;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A utility class made for helping to analyze a given javac {@code TreePath}.
 *
 * @see TreeUtils
 */
public final class TreePathUtil {

    /** Do not instantiate; this class is a collection of static methods. */
    private TreePathUtil() {
        throw new Error("Class TreeUtils cannot be instantiated.");
    }

    /**
     * Gets the first (innermost) enclosing tree in path, of the specified kind.
     *
     * @param path the path defining the tree node
     * @param kind the kind of the desired tree
     * @return the enclosing tree of the given type as given by the path, {@code null} otherwise
     */
    public static @Nullable Tree enclosingOfKind(final TreePath path, final Tree.Kind kind) {
        return enclosingOfKind(path, EnumSet.of(kind));
    }

    /**
     * Gets the first (innermost) enclosing tree in path, with any one of the specified kinds.
     *
     * @param path the path defining the tree node
     * @param kinds the set of kinds of the desired tree
     * @return the enclosing tree of the given type as given by the path, {@code null} otherwise
     */
    public static @Nullable Tree enclosingOfKind(final TreePath path, final Set<Tree.Kind> kinds) {
        TreePath p = path;

        while (p != null) {
            Tree leaf = p.getLeaf();
            assert leaf != null; /*nninvariant*/
            if (kinds.contains(leaf.getKind())) {
                return leaf;
            }
            p = p.getParentPath();
        }

        return null;
    }

    /**
     * Gets path to the first (innermost) enclosing class tree, where class is defined by the
     * classTreeKinds method.
     *
     * @param path the path defining the tree node
     * @return the path to the enclosing class tree, {@code null} otherwise
     */
    public static @Nullable TreePath pathTillClass(final TreePath path) {
        return pathTillOfKind(path, TreeUtils.classTreeKinds());
    }

    /**
     * Gets path to the first (innermost) enclosing tree of the specified kind.
     *
     * @param path the path defining the tree node
     * @param kind the kind of the desired tree
     * @return the path to the enclosing tree of the given type, {@code null} otherwise
     */
    public static @Nullable TreePath pathTillOfKind(final TreePath path, final Tree.Kind kind) {
        return pathTillOfKind(path, EnumSet.of(kind));
    }

    /**
     * Gets path to the first (innermost) enclosing tree with any one of the specified kinds.
     *
     * @param path the path defining the tree node
     * @param kinds the set of kinds of the desired tree
     * @return the path to the enclosing tree of the given type, {@code null} otherwise
     */
    public static @Nullable TreePath pathTillOfKind(
            final TreePath path, final Set<Tree.Kind> kinds) {
        TreePath p = path;

        while (p != null) {
            Tree leaf = p.getLeaf();
            assert leaf != null; /*nninvariant*/
            if (kinds.contains(leaf.getKind())) {
                return p;
            }
            p = p.getParentPath();
        }

        return null;
    }

    /**
     * Gets the first (innermost) enclosing tree in path, of the specified class.
     *
     * @param <T> the type of {@code treeClass}
     * @param path the path defining the tree node
     * @param treeClass the class of the desired tree
     * @return the enclosing tree of the given type as given by the path, {@code null} otherwise
     */
    public static <T extends Tree> @Nullable T enclosingOfClass(
            final TreePath path, final Class<T> treeClass) {
        TreePath p = path;

        while (p != null) {
            Tree leaf = p.getLeaf();
            if (treeClass.isInstance(leaf)) {
                return treeClass.cast(leaf);
            }
            p = p.getParentPath();
        }

        return null;
    }

    /**
     * Gets the enclosing class of the tree node defined by the given {@link TreePath}. It returns a
     * {@link Tree}, from which {@code checkers.types.AnnotatedTypeMirror} or {@link Element} can be
     * obtained.
     *
     * @param path the path defining the tree node
     * @return the enclosing class (or interface) as given by the path, or {@code null} if one does
     *     not exist
     */
    public static @Nullable ClassTree enclosingClass(final TreePath path) {
        return (ClassTree) enclosingOfKind(path, TreeUtils.classTreeKinds());
    }

    /**
     * Gets the enclosing variable of a tree node defined by the given {@link TreePath}.
     *
     * @param path the path defining the tree node
     * @return the enclosing variable as given by the path, or {@code null} if one does not exist
     */
    public static @Nullable VariableTree enclosingVariable(final TreePath path) {
        return (VariableTree) enclosingOfKind(path, Tree.Kind.VARIABLE);
    }

    /**
     * Gets the enclosing method of the tree node defined by the given {@link TreePath}. It returns
     * a {@link Tree}, from which an {@code checkers.types.AnnotatedTypeMirror} or {@link Element}
     * can be obtained.
     *
     * @param path the path defining the tree node
     * @return the enclosing method as given by the path, or {@code null} if one does not exist
     */
    public static @Nullable MethodTree enclosingMethod(final TreePath path) {
        return (MethodTree) enclosingOfKind(path, Tree.Kind.METHOD);
    }

    /**
     * Gets the enclosing method or lambda expression of the tree node defined by the given {@link
     * TreePath}. It returns a {@link Tree}, from which an {@code
     * checkers.types.AnnotatedTypeMirror} or {@link Element} can be obtained.
     *
     * @param path the path defining the tree node
     * @return the enclosing method or lambda as given by the path, or {@code null} if one does not
     *     exist
     */
    public static @Nullable Tree enclosingMethodOrLambda(final TreePath path) {
        return enclosingOfKind(path, EnumSet.of(Tree.Kind.METHOD, Kind.LAMBDA_EXPRESSION));
    }

    /**
     * Returns the top-level block that encloses the given path, or null if none does.
     *
     * @param path a path
     * @return the top-level block that encloses the given path, or null if none does
     */
    public static @Nullable BlockTree enclosingTopLevelBlock(TreePath path) {
        TreePath parentPath = path.getParentPath();
        while (parentPath != null
                && !TreeUtils.classTreeKinds().contains(parentPath.getLeaf().getKind())) {
            path = parentPath;
            parentPath = parentPath.getParentPath();
        }
        if (path.getLeaf().getKind() == Tree.Kind.BLOCK) {
            return (BlockTree) path.getLeaf();
        }
        return null;
    }

    /**
     * Returns true if the tree is in a constructor or an initializer block.
     *
     * @param path the path to test
     * @return true if the path is in a constructor or an initializer block
     */
    public static boolean inConstructor(TreePath path) {
        MethodTree method = enclosingMethod(path);
        // If method is null, this is an initializer block.
        return method == null || TreeUtils.isConstructor(method);
    }

    /**
     * Gets the first (innermost) enclosing tree in path, that is not a parenthesis.
     *
     * @param path the path defining the tree node
     * @return a pair of a non-parenthesis tree that contains the argument, and its child that is
     *     the argument or is a parenthesized version of it
     */
    public static Pair<Tree, Tree> enclosingNonParen(final TreePath path) {
        TreePath parentPath = path.getParentPath();
        Tree enclosing = parentPath.getLeaf();
        Tree enclosingChild = path.getLeaf();
        while (enclosing.getKind() == Kind.PARENTHESIZED) {
            parentPath = parentPath.getParentPath();
            enclosingChild = enclosing;
            enclosing = parentPath.getLeaf();
        }
        return Pair.of(enclosing, enclosingChild);
    }

    /**
     * Returns the tree with the assignment context for the treePath leaf node. (Does not handle
     * pseudo-assignment of an argument to a parameter or a receiver expression to a receiver.)
     *
     * <p>The assignment context for the {@code treePath} is the leaf of its parent, if the parent
     * is one of the following trees:
     *
     * <ul>
     *   <li>AssignmentTree
     *   <li>CompoundAssignmentTree
     *   <li>MethodInvocationTree
     *   <li>NewArrayTree
     *   <li>NewClassTree
     *   <li>ReturnTree
     *   <li>VariableTree
     * </ul>
     *
     * If the parent is a ConditionalExpressionTree we need to distinguish two cases: If the leaf is
     * either the then or else branch of the ConditionalExpressionTree, then recurse on the parent.
     * If the leaf is the condition of the ConditionalExpressionTree, then return null to not
     * consider this assignment context.
     *
     * <p>If the leaf is a ParenthesizedTree, then recurse on the parent.
     *
     * <p>Otherwise, null is returned.
     *
     * @param treePath a path
     * @return the assignment context as described, {@code null} otherwise
     */
    public static @Nullable Tree getAssignmentContext(final TreePath treePath) {
        TreePath parentPath = treePath.getParentPath();

        if (parentPath == null) {
            return null;
        }

        Tree parent = parentPath.getLeaf();
        switch (parent.getKind()) {
            case PARENTHESIZED:
                return getAssignmentContext(parentPath);
            case CONDITIONAL_EXPRESSION:
                ConditionalExpressionTree cet = (ConditionalExpressionTree) parent;
                @SuppressWarnings("interning:not.interned") // AST node comparison
                boolean conditionIsLeaf = (cet.getCondition() == treePath.getLeaf());
                if (conditionIsLeaf) {
                    // The assignment context for the condition is simply boolean.
                    // No point in going on.
                    return null;
                }
                // Otherwise use the context of the ConditionalExpressionTree.
                return getAssignmentContext(parentPath);
            case ASSIGNMENT:
            case METHOD_INVOCATION:
            case NEW_ARRAY:
            case NEW_CLASS:
            case RETURN:
            case VARIABLE:
                return parent;
            default:
                // 11 Tree.Kinds are CompoundAssignmentTrees,
                // so use instanceof rather than listing all 11.
                if (parent instanceof CompoundAssignmentTree) {
                    return parent;
                }
                return null;
        }
    }

    /**
     * Returns whether or not the leaf of the tree path is in a static scope.
     *
     * @param path TreePath whose leaf may or may not be in static scope
     * @return true if the leaf of the tree path is in a static scope
     */
    public static boolean isTreeInStaticScope(TreePath path) {
        MethodTree enclosingMethod = enclosingMethod(path);

        if (enclosingMethod != null) {
            return enclosingMethod.getModifiers().getFlags().contains(Modifier.STATIC);
        }
        // no enclosing method, check for static or initializer block
        BlockTree block = enclosingTopLevelBlock(path);
        if (block != null) {
            return block.isStatic();
        }

        // check if its in a variable initializer
        Tree t = enclosingVariable(path);
        if (t != null) {
            return ((VariableTree) t).getModifiers().getFlags().contains(Modifier.STATIC);
        }
        ClassTree classTree = enclosingClass(path);
        if (classTree != null) {
            return classTree.getModifiers().getFlags().contains(Modifier.STATIC);
        }
        return false;
    }
}
