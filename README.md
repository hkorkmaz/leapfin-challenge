## How to build/run application?

#### Running

```
./run.sh [-n number-of-workers] [-t worker-timeout] [-l log-level] [-h]

     -n   The number of workers, default=10
     -t,  Timeout in seconds for workers, default=60
     -l,  Log level for the application, default=OFF, options : [OFF, ERROR, WARNING, INFO, DEBUG]
     -h,  Help

```

#### Building

```
./build.sh 
```

#### Running Tests
```
./test.sh
```

## Technical Details

* I used Akka Actors as framework. Although same functionality could be implemented without any framework, I chose it to make the application more clean and testable.
* 2 actors defined -> Parent and Worker

#### Parent Actor

- Responsible for creating workers, gathering statistics and printing them
- Messages
    - `Start` -> Starts parent
    - `SaveStats(newStats)` -> Takes new stats as parameter and keep it in memory
    - `GetStats(receiver)` -> Sends stats to receiver
    - `Terminated` -> Internal message when a worker is stopped  



#### Worker Actor

- Responsible for processing stream
- Polls data from stream and searches for `Lpfn` 
- Messages
    - `Start` -> Starts worker
    - `Tick` -> Internal messages for polling stream
    - `Stop` -> Internal message to stop the worker