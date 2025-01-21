# Gitlet Design Document

**Name**: Li Yanzhuo

## Classes and Data Structures

### Main
This is the entry point of our program. It takes in arguments from the command line, and calls the corresponding command in GitletRepository class (which execute the logic of the commands). It also validates he arguments based on the command, to ensure enough arguments were passed in.
[ps. remember the main class should handle errors that don’t apply to any specific commands]

#### Fields

no fields


### Class 2

#### Fields

1. Field 1
2. Field 2


## Algorithms

## Persistence

### .gitlet directory structure

- **CWD**: Current working directory
  - **.gitlet**: Store all the persistent data
      - **HEAD**: The current branch/commit, a single file
      - **index**: The staging area to store the added files
          - It's a single file, a serialized tree map
          - The key is the file path, and the value is blob ID or removal label
      - **commits**: Store the commits
          - **(ab)**: Subdirectory by hash ID
          - **(bc)**
          - **(cd)**
      - **blobs**: Store the blobs
          - **(ab)**: Subdirectory by hash ID
          - **(bc)**
          - **(cd)**
      - **refs**: Store the references
          - **heads**: Store the branches
              - **master**: The default branch
              - **(feature)**
          - **(remotes)**: Store the remote branch


