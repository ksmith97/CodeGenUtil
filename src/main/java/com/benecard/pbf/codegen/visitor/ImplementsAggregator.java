/**
 * 
 */
package com.benecard.pbf.codegen.visitor;

import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

/**
 * @author ksmith_cntr
 *
 */
public class ImplementsAggregator extends VoidVisitorAdapter<List<ClassOrInterfaceType>>
{
    @Override
    public void visit( final ClassOrInterfaceDeclaration d, final List<ClassOrInterfaceType> arg )
    {
        if ( d.getImplements() != null )
        {
            arg.addAll( d.getImplements() );
        }
    }
}