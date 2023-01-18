/// <reference path="codemirror.d.ts" />

interface YASQE_Instance extends CodeMirror.EditorFromTextArea {

	getValue(): string;

	setValue(query: string): void;
}

interface YASQE_Config extends CodeMirror.EditorConfiguration {
	consumeShareLink(): any;
}

interface Token {
	
    /** The character(on the given line) at which the token starts. */
    start: number;

    /** The character at which the token ends. */
    end: number;

    /** The token's string. */
    string: string;

    /** The token type the mode assigned to the token, such as "keyword" or "comment" (may also be null). */
    type: string;

    /** The mode's state at the end of this token. */
    state: any;            
 }

interface Prefixes_Static{

	appendPrefixIfNeeded(yasqe: YASQE_Instance, name: string): void;

	isValidCompletionPosition(yasqe: YASQE_Instance): boolean;

	preprocessPrefixTokenForCompletion(
		yasqe:YASQE_Instance, token: Token): Token;
}

interface Autocompleters_Static {

	prefixes: Prefixes_Static;
}

interface YASQE_Static {

	/** A configuration object of almost arbitrary structure. */
	defaults: any;

	Autocompleters: Autocompleters_Static;
	
	fromTextArea(host: HTMLTextAreaElement,
		options?: YASQE_Config): YASQE_Instance;

	registerAutocompleter(name: string,
		autocompleter:(yasqe:YASQE_Instance, name:string) => any): void;
}

declare module "yasqe" {
	export = YASQE;
}

declare var YASQE: YASQE_Static;