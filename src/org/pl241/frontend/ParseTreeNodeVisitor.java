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
  void enter(ProgramNode node);
  void enter(VarNode node);
  void enter(TypeDeclNode node);
  void enter(NumberNode node);
  void enter(VarDeclNode node);
  void enter(FuncDeclNode node);
  void enter(StatSeqNode node);
  void enter(DesignatorNode node);
  void enter(ExpressionNode node);
  void enter(TermNode node);
  void enter(FactorNode node);
  void enter(RelationNode node);
  void enter(AssignmentNode node);
  void enter(WhileStmtNode node);
  void enter(IfStmtNode node);
  void enter(ReturnStmtNode node);
  void enter(FuncBodyNode node);
  void enter(FormalParamNode node);
  void enter(StatementNode node);
  void enter(FuncCallNode node);
  
  void exit(ProgramNode node);
  void exit(VarNode node);
  void exit(TypeDeclNode node);
  void exit(NumberNode node);
  void exit(VarDeclNode node);
  void exit(FuncDeclNode node);
  void exit(StatSeqNode node);
  void exit(DesignatorNode node) throws Exception;
  void exit(ExpressionNode node);
  void exit(TermNode node);
  void exit(FactorNode node);
  void exit(RelationNode node);
  void exit(AssignmentNode node);
  void exit(WhileStmtNode node);
  void exit(IfStmtNode node);
  void exit(ReturnStmtNode node);
  void exit(FuncBodyNode node);
  void exit(FormalParamNode node);
  void exit(StatementNode node);
  void exit(FuncCallNode node);
}
