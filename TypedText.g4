grammar TypedText;	

compilation_unit: 
            top_defs EOF 
            ;

top_defs : (defun
            |defvars
            |defstruct)*
            ;

defun : typeref name '(' params ')' block ;

defvars
    : type name (',' type name )* ';'
    ;

defstruct
    : STRUCT name member_list ';'
    ;

member_list
    : '{' (slot ';')+ '}'
    ;

slot
    : type name 
    ;

block
    : '{' defvar_list stmts '}'
    ;

defvar_list
    : (defvars)*
    ;

stmts
    : (stmt)*
    ;

type
    :typeref
    ;

// basic types
typeref_base
    : VOID
    | CHAR
    | SHORT
    | INT
    | LONG
    | FLOAT
    | STRUCT IDENTIFIER
    ;

// basic type and array type
typeref
    : typeref_base ('[' (INTEGER_NUM)* ']')*
    ;
// function name is just an ID
name
    : IDENTIFIER
    ;

// can be func(void) or func(int a, float b, struct Point pt)
params
    : VOID
    | param (',' param)*
    ;
// type or typeref
param
    : type name
    ;

stmt
    : ';' // empty statement
    | expr ';'
    | block // embedded block
    | return_stmt
    ;

return_stmt
    : RETURN expr // must return something
    ;

 expr
    : term '=' expr
    | expr2
    ;
 
 expr2
    : expr1 ( ('+' | '-') expr1 )*
    ;

 expr1
    : term (('*' | '/') term)*
    ;

term
    : primary (postfix)*
    ;

postfix
    : '.' name      // structure member
    | '[' expr ']'  // array member reference
    |  '(' args ')' // function call, as primary can be IDENTIFIER
    ;     

args
    :  ( expr (',' expr)* )? // functionall may not have args at all, or 1, 2, many
    ;

primary
    : INTEGER_NUM
    | FLOAT_NUM
    | IDENTIFIER
    | '(' expr ')'
    ;

// lexical rules from here

VOID    : 'void';
CHAR    : 'char';
SHORT   : 'short';
INT     : 'int';
LONG     : 'long';
FLOAT    : 'float';
UNION    : 'union';
STRUCT   : 'struct';
ENUM     : 'enum';
STATIC   : 'static';
EXTERN   : 'extern';
CONST    : 'const';
SIGNED   : 'signed';
UNSIGNED : 'unsigned';
IF       : 'if';
ELSE     : 'else';
SWITCH   : 'switch';
CASE     : 'case';
DEFAULT_ : 'default';
WHILE    : 'while';
DO       : 'do';
FOR      : 'for';
RETURN   : 'return';
BREAK    : 'break';
CONTINUE : 'continue';
GOTO     : 'goto';
TYPEDEF  : 'typedef';
IMPORT   : 'import'; 
SIZEOF   : 'sizeof';

IDENTIFIER 
        : [a-zA-Z][a-zA-Z0-9]*
        ;

INTEGER_NUM     : [0-9]+ ;
FLOAT_NUM      : INTEGER_NUM '.' INTEGER_NUM; 
WS  :   [ \t\n\r]+ -> channel(1); 
