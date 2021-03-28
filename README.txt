This project was created using Reagent and Lein
To run it, you can follow the instructions at https://leiningen.org
Run Lein figwheel and open http://localhost:3449 on your browser.

Note about the application:
1) It's not finished; it doesn't pass all the requirements of the assignment.
2) I will have to admit, the import instruction feed is not very user friendly, for testing / learning purposes.

How to use:
1) The input will take one line of instructions only e.g. "set-stock set-stock AB-6 100 CD-3 200 DE-1 200".
2) Hit the "submit instruction" button (to add to the app-state)
3) Hit the "apply" button to run the instruction

TODO:
- Add file reader and parse multiple lines of instructions
- Apply alphabetical order after feed of instructions
- Add order checks (if not enough stock)
- Add unit tests?
- Clean up
