## Start Folder

### Docs
Contains word docs going through:
  * An intro to Cats Effects & terminology
  * An intro the IO data-type (which is the most used data-type in Cats Effects and has an implementation of all the type-classes in Cats & Cats Effects)
  * An intro to each type-class in Cats Effects

### part1_CustomIO
* We create our own simplified IO data-type (not from Cats) and use this in examples

### part2_IOBasics
Using the Cats Effects IO, shows the basic features and methods IO has, e.g:
  * Chaining IOs together
  * Running IOs on different fibers
  * Higher level functions for running IOs
  * Racing IOs against each other

### part3_ManualFibers
* Showcases the Fiber type (which shouldn't be create manually by the user, as higher-level functions are easier to use)

### part4_Examples
* Showcases example apps using IO