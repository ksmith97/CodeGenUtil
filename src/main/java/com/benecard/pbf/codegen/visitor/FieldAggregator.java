/**
 * 
 */
package com.benecard.pbf.codegen.visitor;

import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.NullLiteralExpr;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

/**
 * @author ksmith_cntr
 *
 */
public class FieldAggregator extends VoidVisitorAdapter<List<FieldDeclaration>>
{
    @Override
    public void visit( final FieldDeclaration d, final List<FieldDeclaration> arg )
    {
        if ( ModifierSet.hasModifier( d.getModifiers(), ModifierSet.PUBLIC )
        || ModifierSet.hasModifier( d.getModifiers(), ModifierSet.PROTECTED ) )
        {
            if ( d.getType() instanceof ReferenceType )
            {
                //We just need the variables to exist to avoid compilation issues.
                for( final VariableDeclarator v : d.getVariables() )
                {
                    v.setInit( new NullLiteralExpr() );
                }
                //Strip comments and annotations
                d.setComment( null );
                d.setAnnotations( null );
            }
            arg.add( d );
        }
    }
}