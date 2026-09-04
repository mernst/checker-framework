# Make a plan for a webpage for every Checker Framework message key

<!-- markdownlint-disable line-length -->
<!-- markdownlint-disable no-bare-urls -->

Make a detailed plan for the following task that an LLM agent can follow.
Write the plan to a file in the current directory.
Ask me questions about anything in this prompt that is unspecified, unclear, or ambiguous, or ill-advised.

The goal of this task is to create, for each error that the Checker Framework may issue, a webpage that explains the message.  The webpage for a message should say what the message means, example code that provokes the error, why the flagged code is problematic, and how to fix it or suppress the warning.  (Sometimes, suppressing the warning is necessary because the Checker Framework does issue false positive warnings; say what the user should verify before suppressing the warning.)
The example code issues only that error, and there should also be a fixed version (that differs slightly in code and/or in annotations) that does not issue the error.  Make the code as realistic as possible while being small: do not write code in bad style just to illustrate an error.

This is inspired by the lists maintained by other analysis tools, which link to their individual webpages.

* Shellcheck: https://www.shellcheck.net/wiki/
* Clippy: https://rust-lang.github.io/rust-clippy/master/
* EsLint: https://eslint.org/docs/latest/rules/
* Error Prone: https://errorprone.info/bugpatterns

Each `messages.properties` file maps from an error message key to user-friendly text.

Here are some tasks that should be included in the plan:

The main work is:
For every error message key (likely batched by checker):

* Create a test case in the appropriate `checker/tests/<CHECKERNAME>/` or `framework/tests/<CHECKERNAME>/` directory, with the name `Example<MESSAGEKEY>.java`.
  This file is both a Checker Framework test case (that is, a legal Java file) and a literate program that contains, interspersed with code (as Java comments, using a markup style that you design), all the text that will appear on the final webpage.  It may also contain text that is invisible/omitted in the final webpage (for example, import statements and other boilerplate).
  The motivations for this approach are:
  * the examples are guaranteed to be up to date, since the test cases must pass.
  * all the information is in a single file rather than spread across multiple files.

You need to design and specify the file format for the examples.
This will serve both the script that processes the examples (see immediately below)
and it will be documentation for people who want to create their own examples or edit existing ones.

Create a script in @checker/bin-devel/ that processes all the examples to create the webpages.
