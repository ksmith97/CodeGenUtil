/**
 * 
 */
package com.benecard.pbm.codegen;

import japa.parser.ParseException;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.type.WildcardType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author ksmith_cntr
 * 
 */
public class ImportResolver
{
    private static final Logger logger = LoggerFactory.getLogger( ImportResolver.class );

    public static Set<String> resolveImports( final Collection<ImportDeclaration> imports, final Collection<Type> types )
    {
        if ( imports == null || imports.size() == 0 || types == null || types.size() == 0 )
        {
            return Sets.newHashSetWithExpectedSize( 0 );
        }
        return new ImportResolver( imports ).resolve( types );
    }

    private final Map<String, String> cache;
    private final Set<ImportDeclaration> imports;

    public ImportResolver( final Collection<ImportDeclaration> d )
    {
        if ( d != null )
        {
            this.imports = Sets.newHashSet( d );
        }
        else
        {
            this.imports = Sets.newHashSetWithExpectedSize( 0 );
        }
        this.cache = Maps.newHashMap();
    }

    public final ClassOrInterfaceType checkTypeArg( final Type t )
    {
        ClassOrInterfaceType coit = null;

        if ( t instanceof ReferenceType )
        {
            final ReferenceType refType = (ReferenceType) t;
            if ( refType.getType() instanceof ClassOrInterfaceType )
            {
                coit = (ClassOrInterfaceType) refType.getType();
            }
        }
        else if ( t instanceof ClassOrInterfaceType )
        {
            coit = (ClassOrInterfaceType) t;
        }

        return coit;
    }


    private String getImportForType( final ClassOrInterfaceType classType )
    throws ParseException
    {
        final String typeStr = classType.getName().trim();
        if ( this.isFullyQualifiedType( typeStr ) )
        {
            return typeStr;
        }
        else
        {
            final String cacheValue = this.cache.get( typeStr );
            if ( cacheValue != null )
            {
                return cacheValue;
            }

            for( final ImportDeclaration dec : this.imports )
            {
                if ( dec.getName().getName().equals( typeStr ) )
                {
                    final String value = dec.toString().trim();
                    this.cache.put( typeStr, value );
                    return value;
                }
            }
        }

        throw new ParseException( "Could not find import for type: " + typeStr );
    }

    private Set<String> getImportsForType( final ClassOrInterfaceType type )
    {
        final Set<String> imports = Sets.newHashSet();

        for( final ClassOrInterfaceType classType : this.resolveTypes( type ) )
        {
            try
            {
                imports.add( this.getImportForType( classType ) );
            }
            catch( final ParseException e )
            {
                ImportResolver.logger.warn( "Failed to retrieve import for type " + classType
                    + " this may because it is in java.lang." );
            }
        }

        return imports;
    }

    /**
     * Predicate to determine if a type is fully qualified.
     * Its currently a hack as it does not distinguish between fully and partial qualification.
     * 
     * @param type
     * @return
     */
    private boolean isFullyQualifiedType( final String type )
    {
        return type.contains( "." );
    }

    public final Set<String> resolve( final Collection<Type> t )
    {
        if ( this.imports.size() == 0 )
        {
            return Sets.newHashSetWithExpectedSize( 0 );
        }

        final Set<String> types = Sets.newHashSet();
        for( final Type type : t )
        {
            types.addAll( this.resolve( type ) );
        }

        return types;
    }

    public final Set<String> resolve( final Type t )
    {
        if ( this.imports.size() == 0 )
        {
            return Sets.newHashSetWithExpectedSize( 0 );
        }

        final ClassOrInterfaceType coit = this.checkTypeArg( t );

        if ( coit == null )
        {
            return Sets.newHashSetWithExpectedSize( 0 );
        }

        return this.getImportsForType( coit );
    }

    private List<ClassOrInterfaceType> resolveTypes( final ClassOrInterfaceType type )
    {
        final List<ClassOrInterfaceType> types = Lists.newArrayList();
        types.add( type );

        if ( type.getTypeArgs() != null )
        {
            for( final Type t : type.getTypeArgs() )
            {
                types.addAll( this.resolveTypes( t ) );
            }
        }

        return types;
    }

    /**
     * This function explodes the type with possible generic arguments into a list of all the ClassOrInterfaceTypes
     * found.
     * We only care about ClassOrInterfaceTypes since they are the only ones that may need importing.
     * 
     * @param type
     * @return
     */
    private List<ClassOrInterfaceType> resolveTypes( final Type type )
    {
        final List<ClassOrInterfaceType> types = Lists.newArrayListWithCapacity( 0 );
        if ( type instanceof ReferenceType )
        {
            final ReferenceType refType = (ReferenceType) type;

            if ( refType.getType() instanceof ClassOrInterfaceType )
            {
                types.addAll( this.resolveTypes( (ClassOrInterfaceType) refType.getType() ) );
            }
        }
        else if ( type instanceof ClassOrInterfaceType )
        {
            types.addAll( this.resolveTypes( (ClassOrInterfaceType) type ) );
        }
        else if ( type instanceof WildcardType )
        {
            final WildcardType wildType = (WildcardType) type;
            if ( wildType.getExtends().getType() instanceof ClassOrInterfaceType )
            {
                types.addAll( this.resolveTypes( (ClassOrInterfaceType) wildType.getExtends().getType() ) );
            }
        }

        return types;
    }
}
