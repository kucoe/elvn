elvn
====

"Laziness is nothing more than the habit of resting before you get tired." Jules Renard

elvn - time management for nerds.

We are living in the world where everything is routinely organized and ready for accounting report. We are surrounded with calendars, planners, bug-trackers, to-do lists, metrics, and every tool or methodology tries to impose its concepts of what is important. elvn is attempting to find out new answers for the common time planning questions and to transform a working day from mechanical execution of "very important" tasks into an amazing journey where inspirational ideas become true. In addition, elvn is a convenient tool that helps to manage your own time avoiding any excess.

The 3 time planning questions are :
  
  1. What should I do?
  2. What am I going to do today?
  3. How should I focus on a task?

elvn answers
  
  1. Everything is an idea, note your idea and if it's worth of doing and could be estimated move it to the task list.
  2. Plan to have 4-6 intentions to embody ideas, each idea should not take more than an hour.
  3. Select a task, have a rest first, be elvn for an hour and move to the next task.


## Installation

  `npm install -g elvn`


## Main elvn commands

  - /b(lue) - switches to task list by color
  - /work - switches to task list by label
  - /all - all tasks
  - /done - completed tasks
  - /today - planned today tasks
  - @ - shows ideas list
  - & - shows lists edit
  - % - shows config (sync)
  - $ - shows timer
  - ! - shows status
  - ? - searches tasks/idea by text
  - \# - gets task/idea by position in list

  examples : 
    - ?correct - returns all tasks/ideas in list that contain 'correct' in text
    - \#2 - returns task that has the second position in list

  With returned task(s) the following can be done: 
    
    #### Command to save/edit task
      - c(olor):text
      - :text

      - \#2=Need to update examples
      - \#2=+test - appends 'test' to task on the second position
      - \#2=-test - removes 'test' substring from task on the second position

    #### Replace text
      
      - \#3=correct%fixed - will change 'correct' to 'fixed' in the third task in list

    #### Move to another list
      
      - \#2b
    
## Task commands
      
  - \#2x- delete
  - \#2> - run
  - \#2v - make completed
  - \#2^ - make not completed
  - \#2+ - make planned
  - \#2- - make unplanned
  - \#2@ - convert task to idea
     
  - \#2-4v - process command over range
  - \#2,3,4v - process command over group
  - \#*v - process command over all tasks
    
## Idea commands

  - \#2x- delete
  - \#2y - convert idea to task and put it to yellow list
       
  - \#2-4x - process command over range
  - \#2,3,4x - process command over group
  - \#*x - process command over all ideas

## Timer commands
      
  - $> - run/resume timer
  - $: - pause timer
  - $x - cancel timer
  - $v - complete task
  - $^ - skip current stage

## Sync commands
      
  - %> - push to remote
  - %< - pull from remote
  - %- - revert last change

## elvn rules:

  - Start with a task and relax for 11 mins.
  - Work for 15 mins.
  - Break for 2 mins.
  - Work for 15 mins.
  - Break for 2 mins.
  - Work for 15 mins.

  Once elvn completed start new.
