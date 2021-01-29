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
locals [int idx=0, List<Integer> indices=new ArrayList<Integer>()]
    : type vars+=name ('=' {$indices.add($idx);} inits+=expr)? (',' {$idx++;} vars+=name ('=' {$indices.add($idx);} inits+=expr)? )* ';'
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
    | STRING                # stringType
    | STRUCT IDENTIFIER     # structType
    ;

// basic types and array types
// for arrays, when you define a variable of array table, you have to specify the array dimensions, i.e. int[3][3]
// but for functions, it is allowed to have a parameter of array type without specific dimensions, i.e. float average(int[] scores, length)
// however, if we support int[] as return type of a function, we have to allow the definition of empty dimension arrays
// for example, int[] scores = getAllScores();
// NO point type: follow the trend in Java and C#, structure can be "reference" type by definition. for example:
// struct TreeNode{
//      int payLoad;
//      struct TreeNode left;
//      struct TreeNode right;
//  }
//  in C/C++, this is cyclic difinition, but allowed in Java.
typeref
locals [int dimCount=0;]
    : typeref_base ('['{$dimCount++;} dimens += INTEGER_NUM? ']')*
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
    : IF '(' expr ')' tstmt=stmt (ELSE fstmt=stmt)?
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

// prefix, such as unary operators, is useful, for example, a= -4; without unary -, one has to write a = 0 - 4;
// this definition only allows one optional prefix, -4 or -a[3] is ok, but not +4, or --1
// cast should come before the unary operator, for example, a = (int)-3.14, or int a = (int)(f/2.0);
// note: (int)(float)b is not considered legal
term
    : ('(' typeCast = type ')')? prefix=('-' | '!')? primary_term
    ;

primary_term
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
    | STRING_LITERAL    #stringLit
    | CHAR_LITERAL      #charLit
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
STRING   : 'string';
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
CHAR_LITERAL:       '\'' (~['\\\r\n] | EscapeSequence) '\'';
STRING_LITERAL:     '"' (~["\\\r\n] | EscapeSequence)* '"';

WS  :   [ \t\n\r]+ -> channel(1); 
SL_COMMENT
    :   '//' .*? '\n' -> channel(2); // channel(COMMENTS)   // single line comment

fragment EscapeSequence
    : '\\' [btnfr"'\\]
    | '\\' ([0-3]? [0-7])? [0-7]
    | '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit
    ;

fragment HexDigit
    : [0-9a-fA-F]
    ;