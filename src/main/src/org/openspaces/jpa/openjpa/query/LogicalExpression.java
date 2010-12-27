package org.openspaces.jpa.openjpa.query;

import org.apache.openjpa.kernel.exps.Expression;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.openspaces.jpa.openjpa.query.BinaryExpression.ExpressionType;

/**
 * Represents a logical expression (AND/OR) in the translated query expression tree.
 * 
 * @author idan
 * @since 8.0
 *
 */
public class LogicalExpression implements Expression, ExpressionNode {
    //
    private static final long serialVersionUID = 1L;
    private ExpressionNode _expression1;
    private ExpressionNode _expression2;
    private ExpressionType _expressionType;
            
    public LogicalExpression(Expression expression1, Expression expression2, ExpressionType expressionType) {
        _expression1 = (ExpressionNode) expression1;
        _expression2 = (ExpressionNode) expression2;
        _expressionType = expressionType;
    }

    public void appendSql(StringBuilder sql) {
        boolean paren = _expressionType == ExpressionType.OR;
        if (paren)
            sql.append("(");
        _expression1.appendSql(sql);
        // When OpenJPA's JPQL parser identifies a collection binding
        // it adds a dummy expression wrapped by an AND expression which later translates
        // to a "1 = 1" expression.
        // We identify such a scenario and don't append the "AND 1 = 1" to the SQL buffer.
        if (_expression2.getNodeType() != NodeType.VARIABLE_BINDING || _expressionType != ExpressionType.AND) {
            sql.append(this.toString());
            _expression2.appendSql(sql);
        }
        if (paren)
            sql.append(")");
    }    
    
    public void acceptVisit(ExpressionVisitor visitor) {
    }

    @Override
    public String toString() {
        switch (_expressionType) {
        case AND:
            return " AND ";
        case OR:
            return " OR ";
        }
        return "";
    }

    public NodeType getNodeType() {
        return NodeType.LOGICAL_EXPRESSION;
    }

    
}
