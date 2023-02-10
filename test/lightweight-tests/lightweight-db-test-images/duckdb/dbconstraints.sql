DROP TABLE IF EXISTS dbconstraints.Book;
CREATE TABLE dbconstraints.Book (
  bk_code int NOT NULL,
  bk_title varchar(100) DEFAULT NULL,
  PRIMARY KEY (bk_code)
);

DROP TABLE IF EXISTS dbconstraints.Writer;
CREATE TABLE dbconstraints.Writer (
                          wr_code int NOT NULL,
                          wr_name varchar(100) DEFAULT NULL,
                          PRIMARY KEY (wr_code)
);

DROP TABLE IF EXISTS dbconstraints.BookWriter;
CREATE TABLE dbconstraints.BookWriter (
  wr_code int NOT NULL,
  bk_code int NOT NULL,
  CONSTRAINT FK_BOOK_WRITER FOREIGN KEY (bk_code) REFERENCES dbconstraints.Book (bk_code) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FK_WRITER_BOOK FOREIGN KEY (wr_code) REFERENCES dbconstraints.Writer (wr_code) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX FK_WRITER_BOOK_idx ON dbconstraints.BookWriter (wr_code);
CREATE INDEX FK_BOOK_WRITER_idx ON dbconstraints.BookWriter (bk_code);

DROP TABLE IF EXISTS dbconstraints.Edition;
CREATE TABLE dbconstraints.Edition (
  ed_code int NOT NULL,
  ed_year int DEFAULT NULL,
  bk_code int DEFAULT NULL,
  PRIMARY KEY (ed_code),
  CONSTRAINT FK_BOOK_EDITION FOREIGN KEY (bk_code) REFERENCES dbconstraints.Book (bk_code) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX FK_BOOK_EDITION_idx ON dbconstraints.Edition (bk_code);

