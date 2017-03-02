--------------------------------------------------------
--  File created - Tuesday-February-21-2017
--------------------------------------------------------

/****** Object:  Database [books]    Script Date: 19.01.17 14:47:03 ******/
CREATE DATABASE [booksdb];
GO
USE [booksdb];
GO
--------------------------------------------------------
--  DDL for Table BOOKS
--------------------------------------------------------

  CREATE TABLE [dbo].[BOOKS]
   (	[ID] [int],
	[TITLE] [varchar](100),
	[PRICE] [decimal](8,2),
	[DISCOUNT] [decimal](8,2),
	[DESCRIPTION] [varchar](100),
	[LANG] [varchar](100),
	[PUBLICATION_DATE] [datetime],
	CONSTRAINT [address_pkey] PRIMARY KEY CLUSTERED
(
	[id] ASC
)
   ) ;
   GO

Insert  [BOOKS] ([ID],[TITLE],[PRICE],[DISCOUNT],[DESCRIPTION],[LANG],[PUBLICATION_DATE]) values ('1','SPARQL Tutorial','42.5',CAST('0.2' AS Decimal (8,2)),'good','en', CAST('05-JUN-14 18:47:52' AS DateTime));
Insert  [BOOKS] ([ID],[TITLE],[PRICE],[DISCOUNT],[DESCRIPTION],[LANG],[PUBLICATION_DATE]) values ('2','The Semantic Web','23',CAST('0.25' AS Decimal (8,2)),'bad','en', CAST('08-DEC-11 12:30:00' AS DateTime));
Insert  [BOOKS] ([ID],[TITLE],[PRICE],[DISCOUNT],[DESCRIPTION],[LANG],[PUBLICATION_DATE]) values ('3','Crime and Punishment','33.5',CAST('0.2' AS Decimal (8,2)),'good','en', CAST('21-SEP-15 09:23:06' AS DateTime));
Insert  [BOOKS] ([ID],[TITLE],[PRICE],[DISCOUNT],[DESCRIPTION],[LANG],[PUBLICATION_DATE]) values ('4','The Logic Book: Introduction, Second Edition','10',CAST('0.15' AS Decimal (8,2)),'good','en', CAST('05-NOV-67 07:50:00'AS DateTime));

