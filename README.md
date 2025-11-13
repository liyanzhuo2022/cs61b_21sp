# **CS61B – Data Structures (Spring 2021 Projects)**

This repository contains my implementations of the three major programming projects from **UC Berkeley’s CS61B: Data Structures**, a rigorous course focused on algorithmic thinking, data structures, and large-scale Java software engineering.

All projects are implemented in Java and emphasize clean abstractions, modular design, testing, and performance analysis.

---

## 📦 **Projects Overview**

---

## **1. 2048 Game (Project 0)**

A full implementation of the classic **2048** puzzle game.

### **Skills & Concepts**

- 2D grid data structures
- Directional state transitions
- Game logic (merge rules, tilt behavior, scoring)
- Encapsulation & object-oriented design
- Model–view separation

### **Highlights**

- Implemented a complete game engine following strict specifications
- Designed a clean Board API for deterministic moves
- Ensured correctness for all four movement directions

---

## **2. Guitar Hero – Custom Deque + Application (Project 1)**

A two-part project:

**(1) building two fully functional deque implementations**, and

**(2) using them to power a simple Guitar Hero–style sound synthesizer.**

### **Data Structure Portion**

- Implemented `LinkedListDeque` (doubly-linked list)
- Implemented `ArrayDeque` (array-based circular buffer)
- Created `MaxArrayDeque` supporting comparator-based max extraction
- Performed **randomized testing and timing benchmarks** to verify correctness and performance

### **Application Portion**

- Reused the custom deque as the internal buffer for a guitar-string simulator
- Integrated with the provided Karplus–Strong framework to generate plucking effects
- Demonstrated separation of concerns: DS implementation vs. application logic

### **Highlights**

- Designed two production-quality deque data structures
- Passed extensive autograder tests for correctness and style
- Built a real application directly on top of self-written data structures

---

## **3. Gitlet – A Mini Version Control System (Project 2)**

A full-scale reimplementation of a simplified **Git** system, and the most complex project in the course.

### **Skills & Concepts**

- Persistent data structures & serialization
- Directed acyclic graphs (DAG) for commit history
- File system operations
- Branching, merging, conflict resolution
- SHA-1 hashing
- Command-line application architecture

### **Highlights**

- Implemented major Git commands:
    
    `init`, `add`, `commit`, `log`, `status`, `branch`, `checkout`, `reset`, `merge`
    
- Built a modular and maintainable codebase with layered abstractions
- Developed a custom persistence layer using serialized commit and blob objects
- Demonstrated strong system design and state management skills

This project closely resembles real production backend engineering: tree-structured data, versioning, persistence, and command parsing.

---

## 🛠 **Technologies & Concepts Used**

- **Java (OOP, interfaces, generics)**
- **Core Data Structures:**
    - Arrays, Linked Lists
    - Deques, ArrayRingBuffer
    - Trees, Tries
    - Hash Maps
    - Priority Queues
    - Graphs & DAG traversal
- **Algorithm Design & Testing:**
    - Randomized tests
    - Timing tests
- **Serialization & file persistence**
- **Command-line application development**
- **Clean API & modular architecture**
