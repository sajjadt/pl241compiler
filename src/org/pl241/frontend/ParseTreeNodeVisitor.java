package org.pl241.frontend;

import org.pl241.frontend.Parser.AssignmentNode;
import org.pl241.frontend.Parser.DesignatorNode;
import org.pl241.frontend.Parser.ExpressionNode;
import org.pl241.frontend.Parser.FactorNode;
import org.pl241.frontend.Parser.FormalParamNode;
import org.pl241.frontend.Parser.FuncBodyNode;
import org.pl241.frontend.Parser.FuncCallNode;
import org.pl241.frontend.Parser.FuncDeclNode;
import org.pl241.frontend.Parser.IfStmtNode;
import org.pl241.frontend.Parser.NumberNode;
import org.pl241.frontend.Parser.ProgramNode;
import org.pl241.frontend.Parser.RelationNode;
import org.pl241.frontend.Parser.ReturnStmtNode;
import org.pl241.frontend.Parser.StatSeqNode;
import org.pl241.frontend.Parser.StatementNode;
import org.pl241.frontend.Parser.TermNode;
import org.pl241.frontend.Parser.TypeDeclNode;
import org.pl241.frontend.Parser.VarDeclNode;
import org.pl241.frontend.Parser.VarNode;
import org.pl241.frontend.Parser.WhileStmtNode;

public interface ParseTreeNodeVisitor
{
  public void enter(ProgramNode node);
  public void enter(VarNode node);
  public void enter(TypeDeclNode node);
  public void enter(NumberNode node);
  public void enter(VarDeclNode node);
  public void enter(FuncDeclNode node);
  public void enter(StatSeqNode node);
  public void enter(DesignatorNode node);
  public void enter(ExpressionNode node);
  public void enter(TermNode node);
  public void enter(FactorNode node);
  public void enter(RelationNode node);
  public void enter(AssignmentNode node);
  public void enter(WhileStmtNode node);
  public void enter(IfStmtNode node);
  public void enter(ReturnStmtNode node);
  public void enter(FuncBodyNode node);
  public void enter(FormalParamNode node);
  public void enter(StatementNode node);
  public void enter(FuncCallNode node);
  
  public void exit(ProgramNode node);
  public void exit(VarNode node);
  public void exit(TypeDeclNode node);
  public void exit(NumberNode node);
  public void exit(VarDeclNode node);
  public void exit(FuncDeclNode node);
  public void exit(StatSeqNode node);
  public void exit(DesignatorNode node) throws Exception;
  public void exit(ExpressionNode node);
  public void exit(TermNode node);
  public void exit(FactorNode node);
  public void exit(RelationNode node);
  public void exit(AssignmentNode node);
  public void exit(WhileStmtNode node);
  public void exit(IfStmtNode node);
  public void exit(ReturnStmtNode node);
  public void exit(FuncBodyNode node);
  public void exit(FormalParamNode node);
  public void exit(StatementNode node);
  public void exit(FuncCallNode node);
}
