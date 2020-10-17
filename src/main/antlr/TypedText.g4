grammar TypedText;	

compilation_unit: 
            topDefs+=top_def* EOF
            ;

top_def : defun         # defunction
        | defvars       # defvariables
        | defstruct     # defstructure
          ;

defun : typeref name '(' params ')' block ;

defvars
    : type vars+=name (','  vars+=name )* ';'
    ;

defstruct
    : STRUCT name member_list ';'
    ;

member_list
    : '{' (slots+=slot ';')+ '}'
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
    : VOID                  # voidType
    | CHAR                  # charType
    | SHORT                 # shortType
    | INT                   # intType
    | LONG                  # longType
    | FLOAT                 # floatType
    | STRUCT IDENTIFIER     # structType
    ;

// basic types and array types
typeref
    : typeref_base ('[' dimens += INTEGER_NUM ']')*
    ;
// function name is just an ID
name
    : IDENTIFIER
    ;

// can be func(void) or func(int a, float b, struct Point pt)
params
    : VOID                  # paramEmpty
    | plist+=param (',' plist+=param)*    # paramList
    ;
// type or typeref, need to allow passing array, i.e. in function
// int average(int[] scores, int length), scores can be an array.
param
    : type name
    ;

stmt
    : ';'           #eptStmt    // empty statement
    | expr ';'      #exprStmt
    | block         #blockStmt  // embedded block
    | return_stmt   #rtnStmt
    | if_stmt       #ifStmt
    | while_stmt    #whileStmt
    ;

while_stmt
    : WHILE '(' expr ')' stmt
    ;

if_stmt
    : IF '(' expr ')' stmt (ELSE stmt)?
    ;

return_stmt
    : RETURN expr // must return something
    ;

expr
    : term '=' expr     # assign
    | expr5             # subExpr5
    ;
 
expr5
    : left=expr4 ('||' right+=expr4)*
    ;

expr4
    : left=expr3 ('&&' right+=expr3)*
    ;

expr3
    : left=expr2 ( op+=('>' | '<' | '>=' | '<=' | '==' | '!=') right+=expr2)*
    ;

expr2
    : left=expr1 ( op+=('+' | '-') right+=expr1 )*
    ;

expr1
    : left=term ( op+=('*' | '/' | '%') right+=term)*
    ;

term
    : primary (posfixes+=postfix)*
    ;

postfix
    : '.' name      #structMember // structure member
    | '[' expr ']'  #arrayMember // array member reference
    |  '(' args ')' #funCall // function call, as primary can be IDENTIFIER
    ;     

args
    :  ( funArgs+=expr (',' funArgs+=expr)* )? // functionall may not have args at all, or 1, 2, many
    ;

primary
    : INTEGER_NUM   #intLit
    | FLOAT_NUM     #floatLit
    | IDENTIFIER    #primarId
    | '(' expr ')'  #primaryExp
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
        : [a-zA-Z]([a-zA-Z0-9] | '_' )*
        ;

INTEGER_NUM     : [0-9]+ ;
FLOAT_NUM      : INTEGER_NUM '.' INTEGER_NUM; 
WS  :   [ \t\n\r]+ -> channel(1); 
SL_COMMENT
    :   '//' .*? '\n' -> channel(2); // channel(COMMENTS)   // single line comment
