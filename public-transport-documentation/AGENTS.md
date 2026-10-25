# Documentation — public-transport-documentation

AsciiDoc manuals built with the Asciidoctor Maven plugin. Two books: a user guide and a technical guide. Prose is Polish.

## Run

From the repo root:

```bash
mvn -pl public-transport-documentation generate-resources
```

The same goal from inside `public-transport-documentation` works too.

Outputs:

- `target/docs/user/main.html` and `main.pdf`
- `target/docs/technical/main.html` and `main.pdf`
- `public-transport-web-client/src/app/generated-doc/` — user-guide HTML and images. The Angular app shows `generated-doc/main.html` in an iframe on the documentation screen. That directory is gitignored. Do not edit it. Rebuild this module to refresh it.

`target/` is build output. Edit only `src/docs/`.

## Layout

- `src/docs/user/main.adoc` — user manual. Include a new chapter from here.
- `src/docs/user/chapters/<topic>/` — one chapter file, images beside it (`chapters/route/images/`).
- `src/docs/technical/main.adoc` — technical manual.
- `src/docs/technical/chapters/` — chapter files. Shared figures live in `src/docs/technical/images/`.

A chapter is an included fragment, not a second book. The book title and `:doctype: book` stay in `main.adoc`.

## Writing

Match the heading depth of the book you are editing. In the user manual, `main.adoc` uses `==` for a part and the chapter file starts at `===`.

User-guide voice is second person and names the screen the reader is on. Put UI labels in bold (`*Dodaj nową linię*`). Explain a field in a table: column `Pole`, column `Znaczenie`. Put a screenshot under a caption:

```asciidoc
.Ekran dodawania nowej linii
image::chapters/route/images/01_nowa_linia.png[Ekran „Dodajesz nową linię”,pdfwidth=100%]
```

Technical chapters use the same AsciiDoc. Commands go in a `[source,bash]` listing. Keep existing file names, including `_digital-oceanl.adoc`.

Describe only screens and behavior that exist in `public-transport-web-client`. Role names in the user guide match `PreAgencyAuthorize.Role` on the server (`AGENCY_OWNER`, `AGENCY_ADMIN`, `PLANNER`, `BRIGADIER`, `DISPATCHER`, `DRIVER`).

## Leave untouched

- `target/`
- `public-transport-web-client/src/app/generated-doc/`
- Hostnames, VPC addresses, and database names already written in the technical chapters. Do not add new secrets, passwords, or tokens.
