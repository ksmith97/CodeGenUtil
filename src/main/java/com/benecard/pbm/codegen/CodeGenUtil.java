/**
 * 
 */
package com.benecard.pbm.codegen;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.type.VoidType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.benecard.pbf.codegen.visitor.ClassNameRetriever;
import com.benecard.pbf.codegen.visitor.FieldAggregator;
import com.benecard.pbf.codegen.visitor.ImplementsAggregator;
import com.benecard.pbf.codegen.visitor.MethodAggregator;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author ksmith_cntr
 *
 */
public class CodeGenUtil
{
    public static class ParameterComparator implements Comparator<MethodDeclaration>
    {
        @Override
        public int compare( final MethodDeclaration o1, final MethodDeclaration o2 )
        {
            final int o1ParamsSize = o1.getParameters() == null ? 0 : o1.getParameters().size();
            final int o2ParamsSize = o2.getParameters() == null ? 0 : o2.getParameters().size();
            if ( !o1.getName().equals( o2.getName() ) )
            {
                return o1.getName().compareTo( o2.getName() );
            }
            else if ( o1ParamsSize != o2ParamsSize )
            {
                return Integer.valueOf( o1ParamsSize ).compareTo(
                    Integer.valueOf( o2ParamsSize ) );
            }
            else
            {
                return 0; //We may want to do further sorting for when they have the same num of params but I'm leaving it alone for now.
            }
        }
    };

    /**
     * Gets the Class Name for the compilation unit.
     * 
     * @param cu
     * @return
     */
    public static String getClassName( final CompilationUnit cu )
    {
        final List<String> arg = Lists.newArrayList();
        new ClassNameRetriever().visit( cu, arg );
        return arg.get( 0 );
    }

    /**
     * Retrieves all the types that are a part of the method declaration.
     * This allows us to generate a set of needed imports. If you need the types of the thrown
     * excpetions use getMethodTypesWithExceptions
     * 
     * @param d
     * @return
     */
    public static Type getFieldTypes( final FieldDeclaration d )
    {
        return d.getType();
    }

    /**
     * Retrieves all the types that are a part of the field declarations.
     * This allows us to generate a set of needed imports.
     * 
     * @param d
     * @return
     */
    public static Collection<Type> getFieldTypes( final Iterable<FieldDeclaration> fields )
    {
        final Set<Type> types = Sets.newHashSet();
        for( final FieldDeclaration field : fields )
        {
            types.add( CodeGenUtil.getFieldTypes( field ) );
        }
        return types;
    }

    public static Collection<ClassOrInterfaceType> getImplements( final CompilationUnit cu )
    {
        final List<ClassOrInterfaceType> imps = Lists.newArrayList();
        new ImplementsAggregator().visit( cu, imps );
        return imps;
    }

    public static Collection<String> getImports( final Collection<MethodDeclaration> methods )
    {
        final Collection<String> imports = Lists.newArrayList();

        return imports;
    }

    /**
     * Gets all the methods for a Compilation Unit.
     * 
     * @param cu
     * @return
     */
    public static List<MethodDeclaration> getMethods( final CompilationUnit cu )
    {
        final List<MethodDeclaration> methods = Lists.newArrayList();
        new MethodAggregator().visit( cu, methods );
        return methods;
    }

    /**
     * Retrieves all the types that are a part of the method declarations.
     * This allows us to generate a set of needed imports. If you need the types of the thrown
     * excpetions use getMethodTypesWithExceptions
     * 
     * @param d
     * @return
     */
    public static Collection<Type> getMethodTypes( final Iterable<MethodDeclaration> methods )
    {
        final Set<Type> types = Sets.newHashSet();
        for( final MethodDeclaration method : methods )
        {
            types.addAll( CodeGenUtil.getMethodTypes( method ) );
        }
        return types;
    }

    /**
     * Retrieves all the types that are a part of the method declaration.
     * This allows us to generate a set of needed imports. If you need the types of the thrown
     * excpetions use getMethodTypesWithExceptions
     * 
     * @param d
     * @return
     */
    public static Collection<Type> getMethodTypes( final MethodDeclaration d )
    {
        final Collection<Type> types = Sets.newHashSet();
        if ( d.getParameters() != null && !d.getParameters().isEmpty() )
        {
            for( final Parameter p : d.getParameters() )
            {
                types.add( p.getType() );
            }
        }

        if ( ! ( d.getType() instanceof VoidType ) )
        {
            types.add( d.getType() );
        }

        if ( d.getThrows() != null && !d.getThrows().isEmpty() )
        {
            for( final NameExpr e : d.getThrows() )
            {
                types.add( new ClassOrInterfaceType( e.getName() ) );
            }
        }

        return types;
    }

    /**
     * Retrieves all the types that are a part of the method declarations.
     * This allows us to generate a set of needed imports.
     * 
     * @param d
     * @return
     */
    public static Collection<Type> getMethodTypesWithExceptions( final Iterable<MethodDeclaration> methods )
    {
        final Set<Type> types = Sets.newHashSet();
        for( final MethodDeclaration method : methods )
        {
            types.addAll( CodeGenUtil.getMethodTypesWithExceptions( method ) );
        }
        return types;
    }

    /**
     * Retrieves all the types that are a part of the method declaration.
     * This allows us to generate a set of needed imports.
     * 
     * @param d
     * @return
     */
    public static Collection<Type> getMethodTypesWithExceptions( final MethodDeclaration d )
    {
        final Collection<Type> types = CodeGenUtil.getMethodTypes( d );

        if ( d.getThrows() != null )
        {
            for( final NameExpr exception : d.getThrows() )
            {
                types.add( new ClassOrInterfaceType( exception.getName() ) );
            }
        }
        return types;
    }

    public static Collection<FieldDeclaration> getPublicFields( final CompilationUnit cu )
    {
        final List<FieldDeclaration> fields = Lists.newArrayList();
        new FieldAggregator().visit( cu, fields );
        return fields;
    }

    /**
     * Used to load a resource as a String.
     * Handles the problem where, when jar'd, resources are loaded from a different place.
     * 
     * @param resourceName
     * @return
     * @throws IOException
     */
    public static String loadResourceAsString( final String resourceName ) throws IOException
    {
        InputStream in = CodeGenUtil.class.getResourceAsStream( resourceName );
        //Hack! The resource name must start with a / to load when jar'd but must not start with a slash when loaded while testing.
        if ( in == null && resourceName.startsWith( "/" ) )
        {
            in = CodeGenUtil.class.getResourceAsStream( resourceName.substring( 1 ) );
        }

        if ( in == null )
        {
            throw new RuntimeException( "Could not find resource " + resourceName );
        }
        final BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
        try
        {
            final StringBuilder builder = new StringBuilder();
            String line = null;
            while( ( line = reader.readLine() ) != null )
            {
                builder.append( line );
                builder.append( "\n" );
            }
            return builder.toString();
        }
        finally
        {
            reader.close();
        }
    }
}
