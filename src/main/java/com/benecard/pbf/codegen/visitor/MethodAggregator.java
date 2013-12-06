/**
 * 
 */
package com.benecard.pbf.codegen.visitor;

import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

/**
 * @author ksmith_cntr
 *
 */
public class MethodAggregator extends VoidVisitorAdapter<List<MethodDeclaration>>
{
    @Override
    public void visit( final MethodDeclaration d, final List<MethodDeclaration> arg )
    {
        if ( ModifierSet.hasModifier( d.getModifiers(), ModifierSet.PUBLIC )
             || ModifierSet.hasModifier( d.getModifiers(), ModifierSet.PROTECTED ) )
        {
            //This is used to exclude methods inside hopefully marked as private inner classes.
            if ( d.getParentNode() instanceof ClassOrInterfaceDeclaration
            && ModifierSet.hasModifier( ( (ClassOrInterfaceDeclaration) d.getParentNode() ).getModifiers(),
                ModifierSet.PUBLIC ) )
            {
                arg.add( d );
            }
        }
    }
}
