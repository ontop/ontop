
parse 	: sql99 EOF {} ;

sql99 	: SELECT 
 {
 } 
 ;

signature 
	:	ALPHANUM

/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/


SELECT	:	 ('S'|'s')('E'|'e')('L'|'l')('E'|'e')('C'|'c')('T'|'t');

DISTINCT :	('D'|'d')('I'|'i')('S'|'s')('T'|'t')('I'|'i')('N'|'n')('C'|'c')('T'|'t');

FROM	:	('F'|'f')('R'|'r')('O'|'o')('M'|'m');

WHERE 	:	('W'|'e')('H'|'h')('E'|'e')('R'|'r')('E'|'e');

AND 	:	('A'|'a')('N'|'n')('D'|'d');

OR 	:	('O'|'o')('R'|'r');

ORDER 	:	('O'|'o')('R'|'r')('D'|'d')('E'|'e')('R'|'r');

BY 	:	('B'|'b')('Y'|'y');

AS 	:	('A'|'a')('S'|'s');

JOIN 	:	 ('J'|'j')('O'|'o')('I'|'i')('N'|'n');

ON 	:	('O'|'o')('N'|'n');

LEFT 	:	('L'|'l')('E'|'e')('F'|'f')('T'|'t');

RIGHT 	:	('R'|'r')('I'|'i')('G'|'g')('H'|'h')('T'|'t');


ID

fragment ALPHA: ('a'..'z'|'A'..'Z');

fragment DIGIT: '0'..'9'; 

fragment ALPHANUM: (ALPHA|DIGIT);

	
