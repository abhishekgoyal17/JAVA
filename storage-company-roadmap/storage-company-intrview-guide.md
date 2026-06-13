# Storage Company Interview Guide

Generated: 2026-06-14

## Target Companies

Storage-focused and storage-adjacent companies to prepare for:

| Category | Companies |
|---|---|
| Cloud NAS, file, and global file systems | Nasuni, NetApp, Qumulo, VAST Data, WEKA, DDN, CTERA, Panzura |
| Data protection, backup, cyber recovery | Cohesity, Rubrik, Commvault, Veeam, Druva, HYCU, Acronis |
| Enterprise primary storage | Pure Storage, Dell Technologies, HPE, IBM Storage, Hitachi Vantara, Infinidat, Huawei OceanStor |
| Object storage and S3-compatible platforms | MinIO, Cloudian, Scality, Ceph/Red Hat, Wasabi, Backblaze B2, Cloudflare R2 |
| Kubernetes/cloud-native storage | Portworx, OpenEBS, Longhorn/SUSE, Rook/Ceph, DataCore, Ondat, Robin.io |
| Cloud provider storage teams | AWS S3/EBS/EFS/FSx, Google Cloud Storage/Persistent Disk/Filestore, Azure Storage/Disk/Files/NetApp Files, Oracle OCI Storage |
| Storage networking and performance infra | NVIDIA/Mellanox, Cisco, Arista, Broadcom, Lightbits Labs, Excelero/NVIDIA, Kalray, Nebulon |
| Hyperconverged and virtualization storage | Nutanix, VMware vSAN/Broadcom, Scale Computing, StarWind |
| Hardware, SSD, and storage systems | Seagate, Western Digital, Micron, Kioxia, Samsung Semiconductor, iXsystems/TrueNAS, Synology, QNAP |

## Hiring Pattern Based on Interview Experience

Based on interview experience, these storage and cloud-infrastructure companies can pay really well, but their hiring is usually more specific than generic software engineering hiring. They often do not hire only for broad DSA/problem-solving ability the way many general product companies do.

For candidates with 2-3+ years of experience, they commonly expect domain-specific exposure in areas like storage systems, Linux internals, distributed systems, storage protocols, Kubernetes storage, performance debugging, C++, or Go infrastructure work. A strong candidate should be able to connect coding skills with real storage/system-level concepts: why latency spikes happen, how data is replicated or recovered, what happens inside the Linux I/O path, how NFS/iSCSI/NVMe/RDMA differ, and how Kubernetes attaches and mounts persistent storage.

The main takeaway: DSA is still required, but it is not enough by itself. These companies usually prefer candidates who can discuss implementation tradeoffs, production debugging, and storage-domain design decisions with confidence.

This guide is aimed at storage/distributed-systems roles where DSA alone is not enough. The expected signal is: Linux internals, storage protocols, distributed storage design, Kubernetes storage, Go/C++ systems programming, and practical performance debugging.

## Your Provided Core Reading List

These are the links you shared. Treat this as the primary reading list; the extra resources later in the guide are added around these so the prep becomes topic-wise and interview-ready.

| Domain | Read | Why it is useful |
|---|---|---|
| Go in cloud infrastructure | [Why top companies choose Golang for cloud infrastructure](https://itfs.com/blog/why-top-companies-choose-golang-for-cloud-infrastructure/#:~:text=Go%20approaches%20concurrency%20differently.,them%20without%20overwhelming%20your%20system.&text=Deployment%20Simplicity,management%20headaches%2C%20no%20version%20conflicts.) | Explains why Go is common in infra companies: concurrency, deployment, operational simplicity |
| Job requirements | [Software Engineer - Distributed Systems, Storage Protocols](https://builtin.com/job/software-engineer-distributed-systems-storage-protocols/9134652#:~:text=2%E2%80%935%20years%20of%20software,based%20environments%20(must%2Dhave)) | Use this as the benchmark JD for storage protocol/distributed systems prep |
| Go high-throughput processing | [Processing 1 million transactions in Go - Part 1](https://blog.karoko.dev/processing-1-million-transactions-in-under-a-second-using-go-part-1-98c9079375ac) | Practical Go concurrency/data-processing example |
| Go high-throughput processing | [Processing 1 million transactions in Go - Part 2](https://blog.karoko.dev/processing-1-million-transactions-in-under-a-second-using-go-part-2-c075800fe7a4) | Continuation with performance-oriented Go implementation details |
| Operating systems | [Virtual Memory: page tables, TLBs, Linux internals](https://hackingcpp.com/cpp/blogs) | Good OS-level background for storage roles |
| Storage networking | [Storage networking video](https://www.youtube.com/watch?v=HP3Z48VnZjk&t=1336s) | Visual explanation of storage networking concepts |
| Storage networking | [Ethernet switches for storage networks](https://intelligentvisibility.com/ethernet-switches-for-storage-networks) | Useful for understanding storage network design and switch requirements |
| Kubernetes storage | [Kubernetes storage architecture for databases](https://simplyblock.io/glossary/kubernetes-storage-architecture-for-databases/#questions-and-answers) | Maps Kubernetes storage concepts to database workloads |
| Storage performance | [What is IOPS?](https://simplyblock.io/glossary/what-is-iops/) | Core performance vocabulary |
| Storage performance | [IOPS, throughput, latency explained](https://simplyblock.io/blog/iops-throughput-latency-explained/) | Must-read for interview discussions around storage performance |
| Storage protocols | [NVMe/TCP vs NVMe/RoCE](https://simplyblock.io/blog/nvme-tcp-vs-nvme-roce/) | Helps compare TCP/IP and RDMA-based high-performance storage |
| RDMA | [What is RDMA?](https://simplyblock.io/glossary/what-is-rdma/) | Intro to zero-copy/low-latency networking concepts |
| Reliability | [What is erasure coding?](https://simplyblock.io/blog/what-is-erasure-coding-a-shield-against-data-loss/) | Important for durability and storage-efficiency design questions |
| Performance optimization | [Art of storage performance optimization](https://simplyblock.io/blog/art-of-storage-performance-optimization/) | Good applied performance-tuning read |
| Kubernetes NAS | [How to use NAS storage with Kubernetes: NFS, SMB, and iSCSI volumes](https://oneuptime.com/blog/post/2025-12-15-how-to-use-nas-storage-with-kubernetes/view) | Practical protocol usage in Kubernetes |
| Kubernetes basics | [Kubernetes containers](https://kubernetes.io/docs/concepts/containers/) | Foundation for K8s storage/mounting questions |
| AI and storage | [AI storage unlocks Kubernetes performance](https://www.weka.io/article/your-kubernetes-workloads-aren-t-cpu-bound-they-re-waiting-on-storage) | Good context for modern AI storage workload bottlenecks |
| Kubernetes security | [RBAC good practices](https://kubernetes.io/docs/concepts/security/rbac-good-practices/) | Important for CSI driver and storage operator security |
| Kubernetes security | [Multi-tenancy](https://kubernetes.io/docs/concepts/security/multi-tenancy/) | Useful for tenant isolation and storage platform design |
| AI engineering | [ai-engineering-from-scratch](https://github.com/rohitg00/ai-engineering-from-scratch) | Broader AI infra context |
| Helm | [What is Helm in Kubernetes?](https://www.sysdig.com/learn-cloud-native/what-is-helm-in-kubernetes) | Intro to Helm in K8s deployments |
| Operating systems | [OSTEP](https://pages.cs.wisc.edu/~remzi/OSTEP/) | Best long-form OS foundation for systems interviews |

## Extra Topics You Listed Without Specific Links

| Topic | Where it is covered in this guide |
|---|---|
| NFS | Storage protocols, protocol comparison, troubleshooting drills, stale file handle questions |
| Inodes | Linux/OS internals, interview question bank, bug themes |
| Top bugs filed | Top bug and incident themes section |
| IP table / iptables | Storage networking, Linux networking, troubleshooting drills |
| Golang | Go for cloud and storage infrastructure, Go drills, Go question bank |
| C++ / CPP | C++ for storage and systems infrastructure, C++ drills, C++ question bank |

## What Storage Companies Usually Test

| Area | What interviewers look for | Example signal |
|---|---|---|
| DSA and coding | Clean implementation, correctness, complexity, concurrency safety | LRU cache, producer-consumer, rate limiter, log parser, tree/hash-map problems |
| Go for infrastructure | Goroutines, channels, context cancellation, memory/GC awareness, profiling | Build a bounded worker pool with cancellation and backpressure |
| C++ for systems/data plane | RAII, ownership, move semantics, memory layout, concurrency, performance debugging | Implement a thread-safe cache, buffer pool, or async I/O pipeline |
| Linux systems | Files, inodes, page cache, virtual memory, syscalls, sockets, process/thread model | Explain what happens on `read()`, `write()`, `fsync()`, `mmap()` |
| Storage fundamentals | Block vs file vs object, IOPS, latency, throughput, queue depth, data durability | Diagnose why throughput is high but p99 latency is bad |
| Storage protocols | NFS, SMB, iSCSI, NVMe/TCP, NVMe/RoCE, RDMA, S3 API basics | Compare NFS vs iSCSI vs S3 for databases and AI workloads |
| Distributed systems | Replication, erasure coding, consensus, metadata services, failure recovery | Design a distributed file/object store with snapshots |
| Kubernetes storage | PV/PVC/StorageClass/CSI, topology, snapshots, StatefulSets, RBAC | Debug a PVC stuck in `Pending` or a pod stuck mounting a volume |
| Performance debugging | fio, iostat, perf, strace, eBPF, pprof, flame graphs, metrics | Find whether a bottleneck is CPU, network, disk, lock contention, or GC |
| Reliability/security | Snapshots, backup, immutability, encryption, auth, multi-tenancy | Design ransomware-safe backup or tenant-isolated K8s storage |

## Fast Roadmap

| Week | Focus | Outcome |
|---|---|---|
| 1 | Storage basics, Linux filesystems, IOPS/latency/throughput | You can explain storage stacks and benchmark simple workloads |
| 2 | NFS/iSCSI/NVMe/RDMA, storage networking, iptables | You can compare protocols and reason about network latency/failure |
| 3 | Distributed storage, replication, erasure coding, consistency | You can design scalable, durable storage services |
| 4 | Kubernetes storage, CSI, StatefulSets, snapshots, RBAC | You can debug K8s storage issues and explain CSI control/data paths |
| 5 | Go and C++ systems programming, profiling, memory, million-record processing | You can build and tune Go/C++ data pipelines |
| 6 | Mock interviews, troubleshooting drills, project demos | You can discuss real incidents, tradeoffs, and implementation details |

## Domain 1: Storage Fundamentals

### Must Know

| Topic | What to learn |
|---|---|
| Block storage | Logical block devices, sectors, volumes, LUNs, random vs sequential I/O |
| File storage | Filesystems, directories, permissions, inodes, file handles, POSIX semantics |
| Object storage | Buckets, objects, metadata, eventual/strong consistency, S3 API patterns |
| IOPS | Operations per second; depends on block size, queue depth, read/write mix |
| Throughput | Data volume per second; `throughput = IOPS * block_size` |
| Latency | Time per operation; average is not enough, p95/p99 matter |
| Queue depth | Number of outstanding I/O requests; higher can improve throughput but hurt latency |
| Durability | Replication, erasure coding, checksums, scrubbing, snapshots |
| Availability | Failover, quorum, placement, healing, node/rack/AZ failures |
| Consistency | Read-after-write, eventual consistency, metadata consistency, leases/locks |

### Block vs File vs Object

| Type | Interface | Best for | Interview talking points |
|---|---|---|---|
| Block | Raw device/volume | Databases, VM disks, low-level filesystems | Low latency, fixed-size blocks, host filesystem owns metadata |
| File | Path-based filesystem | Shared directories, enterprise NAS, user/home dirs | POSIX semantics, locking, permissions, directory metadata |
| Object | HTTP/API key-value object model | Backups, data lakes, media, logs, AI datasets | Flat namespace, metadata, versioning, lifecycle, scale |

### Key Resources

| Resource | Type | Why it matters |
|---|---|---|
| [What is IOPS?](https://simplyblock.io/glossary/what-is-iops/) | Article | Good starting point for IOPS vocabulary |
| [IOPS, throughput, latency explained](https://simplyblock.io/blog/iops-throughput-latency-explained/) | Article | Interview-friendly performance language |
| [Amazon EBS overview](https://docs.aws.amazon.com/ebs/latest/userguide/what-is-ebs.html) | Official docs | Real cloud block storage features: volumes, snapshots, durability |
| [Amazon S3 overview](https://docs.aws.amazon.com/AmazonS3/latest/userguide/Welcome.html) | Official docs | Object storage model, storage classes, access control |
| [Amazon EFS overview](https://docs.aws.amazon.com/efs/latest/ug/whatisefs.html) | Official docs | Cloud NFS-style shared file storage |
| [Azure Storage introduction](https://learn.microsoft.com/en-us/azure/storage/common/storage-introduction) | Official docs | Azure block/blob/file/queue storage taxonomy |
| [Azure managed disks overview](https://learn.microsoft.com/en-us/azure/virtual-machines/managed-disks-overview) | Official docs | Managed cloud disk concepts |
| [Google Cloud storage strategy guide](https://docs.cloud.google.com/architecture/storage-advisor) | Official docs | Choosing block/file/object storage by workload |

## Domain 2: Linux, OS, and Filesystem Internals

### Must Know

| Topic | Interview depth |
|---|---|
| Virtual memory | Address spaces, page tables, TLB, page faults, mmap, copy-on-write |
| Page cache | Buffered I/O, cache hits/misses, writeback, dirty pages |
| Inodes | File metadata, link count, directories as mappings, inode exhaustion |
| VFS | How Linux abstracts filesystems behind common syscalls |
| Filesystems | ext4, XFS, btrfs basics; journaling; metadata vs data operations |
| Block layer | Request queues, multi-queue block layer, I/O scheduling |
| Direct I/O | `O_DIRECT`, alignment, bypassing page cache, database workloads |
| Sync semantics | `fsync`, `fdatasync`, durability vs visibility |
| Syscalls | `open`, `read`, `write`, `pread`, `pwrite`, `sendfile`, `mmap` |
| Observability | `strace`, `lsof`, `iostat`, `vmstat`, `pidstat`, `perf`, eBPF |

### What Happens When You Read a File?

| Step | Concept |
|---|---|
| 1 | Process calls `read(fd, buf, n)` |
| 2 | Kernel resolves file descriptor to `struct file` |
| 3 | VFS dispatches to filesystem implementation |
| 4 | Kernel checks page cache |
| 5 | On cache miss, filesystem maps file offset to disk blocks/extents |
| 6 | Block layer submits I/O to device/driver |
| 7 | Data is copied from page cache to user buffer |
| 8 | Return value reports bytes read or error |

### Key Resources

| Resource | Type | Why it matters |
|---|---|---|
| [OSTEP: Operating Systems: Three Easy Pieces](https://pages.cs.wisc.edu/~remzi/OSTEP/) | Free book | Best operating systems foundation for interviews |
| [Linux VFS documentation](https://docs.kernel.org/filesystems/vfs.html) | Official docs | How Linux models files, inodes, superblocks, dentries |
| [Linux block layer docs](https://docs.kernel.org/block/index.html) | Official docs | Entry point for block device and I/O scheduler internals |
| [Linux IP sysctl docs](https://docs.kernel.org/networking/ip-sysctl.html) | Official docs | Kernel TCP/IP tuning knobs |
| [Virtual Memory: Page Tables, TLBs, Linux Internals](https://hackingcpp.com/cpp/blogs) | Blog collection | Useful OS deep dives |
| [man7 Linux man pages](https://man7.org/linux/man-pages/) | Reference | Syscalls, sockets, files, process APIs |
| [io_uring overview from kernel tree](https://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git/tree/io_uring) | Source docs/code | Modern async Linux I/O |

## Domain 3: Storage Protocols and Storage Networking

### Protocol Comparison

| Protocol | Type | Transport | Where used | What to know |
|---|---|---|---|---|
| NFS | File | TCP/IP | NAS, Kubernetes RWX volumes, shared Linux dirs | File handles, mounts, locking, client cache, stale file handles |
| SMB/CIFS | File | TCP/IP | Windows/enterprise file sharing | ACLs, shares, authentication, locking |
| iSCSI | Block | TCP/IP | SAN over Ethernet | Initiator/target, LUNs, sessions, multipath |
| Fibre Channel | Block | FC fabric | Enterprise SAN | Low latency, zoning, WWNs, dedicated fabric |
| NVMe/TCP | Block | TCP/IP | Disaggregated NVMe storage | NVMe queues over TCP, standard Ethernet, CPU tradeoffs |
| NVMe/RoCE | Block | RDMA over Ethernet | Low-latency flash storage | RDMA, lossless networking, PFC/ECN, queue pairs |
| S3 API | Object | HTTP/HTTPS | Object storage, backups, data lakes | Buckets, objects, consistency, multipart upload, lifecycle |
| RDMA | Memory/network primitive | RoCE, iWARP, InfiniBand | High-performance storage and HPC | Zero-copy, queue pairs, verbs, memory registration |

### Storage Networking Topics

| Topic | Study notes |
|---|---|
| TCP basics | Windowing, congestion control, retransmits, Nagle, delayed ACKs |
| MTU/jumbo frames | Larger frames can reduce CPU overhead but require end-to-end config |
| Multipathing | Multiple paths for availability/performance; path failover |
| RoCE | RDMA over Converged Ethernet; sensitive to packet loss and fabric config |
| PFC/ECN | Data center lossless/low-loss Ethernet controls |
| Network isolation | VLANs, QoS, separate storage networks, blast-radius control |
| iptables/netfilter | Packet filtering, NAT, debugging connectivity and policies |
| Tail latency | Small packet loss/retransmit spikes can destroy p99 latency |

### Key Resources

| Resource | Type | Why it matters |
|---|---|---|
| [NFSv4 RFC 7530](https://www.rfc-editor.org/info/rfc7530/) | Standard | Authoritative NFSv4 protocol reference |
| [iSCSI RFC 7143](https://datatracker.ietf.org/doc/html/rfc7143) | Standard | Authoritative iSCSI protocol reference |
| [NVM Express specifications](https://nvmexpress.org/specifications/) | Standard hub | NVMe, NVMe/TCP, NVMe over Fabrics specifications |
| [RDMA Aware Programming User Manual](https://networking-docs.nvidia.com/rdmaawareprogramming) | Vendor docs | Practical RDMA verbs and programming model |
| [What is RDMA?](https://simplyblock.io/glossary/what-is-rdma/) | Article | Friendly intro to RDMA concepts |
| [NVMe/TCP vs NVMe/RoCE](https://simplyblock.io/blog/nvme-tcp-vs-nvme-roce/) | Article | Good protocol tradeoff comparison |
| [Ethernet switches for storage networks](https://intelligentvisibility.com/ethernet-switches-for-storage-networks) | Article | Storage networking design considerations |
| [Storage networking YouTube video](https://www.youtube.com/watch?v=HP3Z48VnZjk&t=1336s) | Video | Practical storage networking overview |
| [netfilter/iptables documentation](https://netfilter.org/documentation/) | Official docs | iptables/netfilter learning path |

## Domain 4: Distributed Storage Systems

### Must Know

| Topic | What to understand |
|---|---|
| Data placement | Sharding, consistent hashing, CRUSH-like placement, topology awareness |
| Metadata services | Directory tree, object index, namespace, leases, hot metadata partitions |
| Replication | Leader/follower, quorum, synchronous vs asynchronous writes |
| Erasure coding | Space-efficient durability, read/write amplification, rebuild cost |
| Consensus | Raft/Paxos basics for metadata, config, membership |
| Failure recovery | Re-replication, healing, scrubbing, anti-entropy, bit rot detection |
| Snapshots | Copy-on-write, redirect-on-write, crash consistency, retention |
| Dedup/compression | Content-defined chunking, fingerprints, CPU/memory tradeoffs |
| Tiering | Hot/cold data, object backend, SSD cache, HDD/cloud archive |
| Backpressure | Protect system under overload; admission control and queues |

### Replication vs Erasure Coding

| Design | Pros | Cons | Use cases |
|---|---|---|---|
| 3x replication | Simple, fast reads, fast recovery | 3x space cost | Hot data, metadata, low latency |
| 2x replication | Lower cost than 3x | Lower durability, weaker failure tolerance | Non-critical or small clusters |
| Erasure coding | High durability with lower space overhead | More CPU, network, small-write amplification | Object storage, cold/warm data, backup |
| Hybrid | Tune by workload | More operational complexity | Real storage systems |

### Design Questions to Practice

| Question | What interviewers want |
|---|---|
| Design a distributed object store like S3 | API, metadata, placement, replication/EC, consistency, lifecycle |
| Design a cloud NAS like Nasuni | File caching, global namespace, snapshots, locking, cloud object backend |
| Design a backup platform like Cohesity/Rubrik | Incremental backups, indexing, dedupe, immutability, recovery |
| Design a Kubernetes CSI storage backend | Controller service, node service, attach/mount, topology, snapshots |
| Design a distributed block volume service | Volume metadata, data replication, attach semantics, failover |
| Handle a node failure during writes | Quorum, write-ahead logging, idempotency, recovery |
| Rebalance after adding nodes | Placement, throttling, data movement, user impact |

### Key Resources

| Resource | Type | Why it matters |
|---|---|---|
| [Ceph architecture](https://docs.ceph.com/en/latest/architecture/) | Official docs | Real distributed object/block/file storage architecture |
| [MinIO erasure coding](https://docs.min.io/aistor/operations/core-concepts/erasure-coding/) | Official docs | Practical erasure coding model |
| [Erasure coding article](https://simplyblock.io/blog/what-is-erasure-coding-a-shield-against-data-loss/) | Article | Good interview-level explanation |
| [Art of storage performance optimization](https://simplyblock.io/blog/art-of-storage-performance-optimization/) | Article | Performance tuning themes |
| [Google File System paper](https://research.google/pubs/the-google-file-system/) | Paper | Classic distributed file system design |
| [Dynamo paper](https://www.allthingsdistributed.com/files/amazon-dynamo-sosp2007.pdf) | Paper | Quorum, partitioning, availability-first design |
| [Raft paper](https://raft.github.io/raft.pdf) | Paper | Consensus foundation |
| [MIT 6.824 Distributed Systems](https://pdos.csail.mit.edu/6.824/) | Course | Strong distributed systems interview prep |

## Domain 5: Kubernetes Storage Architecture

### Must Know

| Topic | Interview depth |
|---|---|
| Volume | Data mounted into a pod; lifetime depends on volume type |
| PersistentVolume | Cluster storage resource provisioned statically or dynamically |
| PersistentVolumeClaim | User request for storage; binds to a PV |
| StorageClass | Defines provisioner, parameters, reclaim policy, binding mode |
| CSI | Standard interface between Kubernetes and storage providers |
| StatefulSet | Stable identity, ordered rollout, per-pod PVCs |
| Access modes | RWO, RWOP, ROX, RWX and what they imply |
| Volume snapshots | Snapshot CRDs, backup workflows, crash consistency caveats |
| Topology | Zone/node-aware provisioning, local PVs, delayed binding |
| Security | RBAC, secrets, multi-tenancy, least privilege for controllers |

### Debugging a PVC Stuck in Pending

| Check | Why |
|---|---|
| `kubectl describe pvc` | Events usually show binding/provisioning reason |
| StorageClass exists | Wrong/missing class prevents dynamic provisioning |
| CSI provisioner running | Controller may be down or unauthorized |
| Capacity and access mode | No matching PV or unsupported access mode |
| VolumeBindingMode | `WaitForFirstConsumer` waits for pod scheduling |
| Topology constraints | Zone/node mismatch can block binding |
| RBAC/secrets | CSI driver may lack permissions or credentials |

### Key Resources

| Resource | Type | Why it matters |
|---|---|---|
| [Kubernetes storage architecture for databases](https://simplyblock.io/glossary/kubernetes-storage-architecture-for-databases/#questions-and-answers) | Article | Storage architecture view for DB workloads |
| [Kubernetes volumes](https://kubernetes.io/docs/concepts/storage/volumes/) | Official docs | Volume types and semantics |
| [Kubernetes PersistentVolumes](https://kubernetes.io/docs/concepts/storage/persistent-volumes/) | Official docs | PV/PVC lifecycle |
| [Kubernetes StorageClasses](https://kubernetes.io/docs/concepts/storage/storage-classes/) | Official docs | Dynamic provisioning and storage policy |
| [Kubernetes dynamic provisioning](https://kubernetes.io/docs/concepts/storage/dynamic-provisioning/) | Official docs | How PVCs create volumes |
| [Kubernetes volume snapshots](https://kubernetes.io/docs/concepts/storage/volume-snapshots/) | Official docs | Snapshot CRDs and workflow |
| [CSI specification](https://github.com/container-storage-interface/spec/blob/master/spec.md) | Spec | The core standard for K8s storage drivers |
| [Kubernetes containers](https://kubernetes.io/docs/concepts/containers/) | Official docs | Container basics for storage mounting context |
| [Kubernetes RBAC good practices](https://kubernetes.io/docs/concepts/security/rbac-good-practices/) | Official docs | Least privilege for controllers/operators |
| [Kubernetes multi-tenancy](https://kubernetes.io/docs/concepts/security/multi-tenancy/) | Official docs | Tenant isolation considerations |
| [What is Helm?](https://www.sysdig.com/learn-cloud-native/what-is-helm-in-kubernetes) | Article | Helm concepts |
| [Using Helm](https://helm.sh/docs/intro/using_helm/) | Official docs | Charts, releases, values, upgrades |
| [NAS with Kubernetes: NFS, SMB, iSCSI](https://oneuptime.com/blog/post/2025-12-15-how-to-use-nas-storage-with-kubernetes/view) | Article | Practical protocol examples |

## Domain 6: Go for Cloud and Storage Infrastructure

### Why Go Is Common in Cloud Infrastructure

| Reason | Why storage/cloud companies care |
|---|---|
| Lightweight concurrency | Goroutines map well to many I/O-bound operations |
| Simpler deployment | Static binaries are easy to ship into containers and appliances |
| Good networking libraries | Strong standard library for servers, clients, TLS, HTTP |
| Tooling | `go test`, race detector, pprof, trace, benchmarks |
| Memory safety | Safer than C/C++ for control planes and services |
| Performance | Good enough for many storage control-plane and data-plane services |
| Cloud-native ecosystem | Kubernetes, Docker, Terraform, etcd, Prometheus, and many infra tools use Go |

### Must Know

| Topic | Interview depth |
|---|---|
| Goroutines | Lifecycle, leaks, scheduling, fan-out/fan-in |
| Channels | Ownership, closing rules, buffering, select, backpressure |
| Context | Cancellation, deadlines, request-scoped values |
| sync package | Mutex, RWMutex, Cond, Once, WaitGroup, Pool, atomic |
| Memory model | Happens-before, data races, race detector |
| GC | Heap, allocation pressure, latency, tuning basics |
| pprof | CPU, heap, mutex, block profiles |
| net/http and gRPC | Servers, timeouts, streaming, retries |
| Error handling | Wrapping, sentinel errors, retryable errors |
| Benchmarks | `go test -bench`, allocations, profiles |

### Key Resources

| Resource | Type | Why it matters |
|---|---|---|
| [Why companies choose Go for cloud infrastructure](https://itfs.com/blog/why-top-companies-choose-golang-for-cloud-infrastructure/#:~:text=Go%20approaches%20concurrency%20differently.,them%20without%20overwhelming%20your%20system.&text=Deployment%20Simplicity,management%20headaches%2C%20no%20version%20conflicts.) | Article | Your original Go/cloud motivation link |
| [Effective Go](https://go.dev/doc/effective_go) | Official docs | Idiomatic Go basics |
| [Go diagnostics](https://go.dev/doc/diagnostics) | Official docs | Profiling and debugging tools |
| [Go concurrency pipelines](https://go.dev/blog/pipelines) | Official blog | Cancellation, fan-out/fan-in, pipeline patterns |
| [Go GC guide](https://go.dev/doc/gc-guide) | Official docs | Memory and GC tradeoffs |
| [Go memory model](https://go.dev/ref/mem) | Official docs | Correct concurrent programming |
| [Profiling Go programs](https://go.dev/blog/pprof) | Official blog | pprof workflow |
| [Processing 1 million transactions in Go, Part 1](https://blog.karoko.dev/processing-1-million-transactions-in-under-a-second-using-go-part-1-98c9079375ac) | Article | High-throughput Go pipeline example |
| [Processing 1 million transactions in Go, Part 2](https://blog.karoko.dev/processing-1-million-transactions-in-under-a-second-using-go-part-2-c075800fe7a4) | Article | Continuation of high-throughput Go example |

### Go Coding Drills

| Drill | Concepts tested |
|---|---|
| Bounded worker pool with context cancellation | Goroutines, channels, backpressure |
| Concurrent file checksum calculator | File I/O, worker queues, error handling |
| Rate-limited uploader | Token bucket, retries, HTTP client timeouts |
| Log aggregator with top-K errors | Maps, heaps, concurrency safety |
| In-memory LRU cache with TTL | Data structures, locking, time cleanup |
| Stream 1 million records under memory limit | Buffers, batching, profiling |
| TCP echo server with graceful shutdown | Sockets, goroutines, context |

## Domain 7: C++ for Storage and Systems Infrastructure

Storage companies often expect C++ for performance-sensitive paths: storage engines, protocol implementations, kernel-adjacent services, caching layers, replication pipelines, and high-performance networking. Even if the team uses Go for control-plane work, C++ is common in data-plane components where memory layout, CPU cache behavior, and predictable latency matter.

### Why C++ Matters in Storage Companies

| Reason | Why storage/cloud companies care |
|---|---|
| Low-level memory control | Data buffers, alignment, zero-copy I/O, custom allocators |
| Predictable performance | Avoiding unexpected GC pauses in latency-sensitive paths |
| Mature systems ecosystem | RocksDB, Ceph, Envoy, SPDK, many storage engines and protocol stacks |
| Hardware and kernel proximity | NVMe, RDMA, io_uring, DMA, mmap, filesystem/block abstractions |
| Efficient data structures | Cache-aware indexes, queues, ring buffers, memory pools |
| Concurrency primitives | Threads, atomics, lock-free structures, condition variables |
| ABI/library integration | Easy interop with C libraries, Linux APIs, drivers, and vendor SDKs |

### Must Know

| Topic | Interview depth |
|---|---|
| RAII | Resource lifetime, deterministic cleanup, file descriptors, locks, buffers |
| Ownership | Raw pointer vs `unique_ptr` vs `shared_ptr` vs references |
| Move semantics | Avoid expensive copies in buffers, requests, and large objects |
| Rule of 0/3/5 | Correct copy/move/destructor behavior |
| Memory layout | Stack vs heap, alignment, padding, cache lines, false sharing |
| STL containers | Vector/list/map/unordered_map tradeoffs, iterator invalidation |
| Strings and buffers | `std::string`, `std::string_view`, spans, lifetime pitfalls |
| Concurrency | `std::thread`, mutex, condition variable, future, atomics |
| Atomics | Memory ordering basics, acquire/release, compare-exchange |
| Error handling | Exceptions vs error codes/status objects in systems code |
| Build systems | CMake basics, compiler flags, debug/release builds |
| Debugging | gdb/lldb, sanitizers, valgrind, perf, flame graphs |

### C++ Topics Storage Interviewers Like

| Topic | Example question |
|---|---|
| Buffer ownership | How do you pass a 4 MiB I/O buffer without copying it? |
| Zero-copy I/O | What does zero-copy mean, and when is it not actually zero-copy? |
| Alignment | Why might `O_DIRECT` require aligned buffers? |
| Cache behavior | Why can a linked list be slower than a vector despite O(1) insertion? |
| False sharing | How can two independent counters slow each other down? |
| Object lifetime | What bug can happen when returning `string_view` from a temporary string? |
| Smart pointers | When is `shared_ptr` dangerous in hot paths? |
| Lock contention | How would you reduce contention in a global metadata map? |
| Atomic ordering | Difference between relaxed and acquire/release ordering |
| Exception policy | Why do many storage systems avoid exceptions in hot paths? |

### Key Resources

| Resource | Type | Why it matters |
|---|---|---|
| [cppreference](https://en.cppreference.com/w/) | Reference | Best day-to-day C++ language and standard library reference |
| [C++ Core Guidelines](https://isocpp.github.io/CppCoreGuidelines/CppCoreGuidelines) | Guidelines | Modern C++ safety, ownership, and style principles |
| [Google C++ Style Guide](https://google.github.io/styleguide/cppguide.html) | Style guide | Common in infra-oriented codebases and interviews |
| [CMake tutorial](https://cmake.org/cmake/help/latest/guide/tutorial/index.html) | Build docs | Practical C++ build-system foundation |
| [LLVM AddressSanitizer](https://clang.llvm.org/docs/AddressSanitizer.html) | Tool docs | Detect memory errors, use-after-free, buffer overflows |
| [LLVM ThreadSanitizer](https://clang.llvm.org/docs/ThreadSanitizer.html) | Tool docs | Detect C++ data races |
| [Valgrind manual](https://valgrind.org/docs/manual/manual.html) | Tool docs | Memory debugging and leak detection |
| [Abseil C++ tips/guides](https://abseil.io/docs/cpp/guides/) | Guides | Practical modern C++ patterns from production systems |
| [gRPC C++ docs](https://grpc.io/docs/languages/cpp/) | Docs | Useful for storage control/data-plane RPC services |
| [RocksDB wiki](https://github.com/facebook/rocksdb/wiki) | Project docs | Storage engine concepts in a major C++ codebase |
| [SPDK documentation](https://spdk.io/doc/) | Project docs | User-space high-performance storage and NVMe concepts |

### C++ Coding Drills

| Drill | Concepts tested |
|---|---|
| Thread-safe LRU cache | RAII, mutexes, unordered_map/list, iterator correctness |
| Fixed-size buffer pool | Ownership, alignment, memory reuse, avoiding allocations |
| Producer-consumer queue | Condition variables, shutdown, backpressure |
| Lock-free counter/ring buffer | Atomics, memory ordering, cache lines |
| File copy with large buffers | File descriptors, error handling, partial reads/writes |
| mmap-backed key-value reader | `mmap`, lifetime, page faults, parsing |
| Mini write-ahead log | append, checksum, fsync, recovery |
| Simple TCP server | sockets, thread pool, graceful shutdown |
| Benchmark vector vs list | Cache locality, allocation overhead |
| Parse 1 million records under memory limit | Move semantics, string_view lifetime, profiling |

### C++ Interview Question Bank

| Question | Key points |
|---|---|
| What is RAII? | Tie resource lifetime to object lifetime; destructors clean up |
| `unique_ptr` vs `shared_ptr`? | Single ownership vs shared ownership; refcount cost and cycles |
| What is move semantics? | Transfer resources without deep copy; moved-from object validity |
| Vector vs list? | Contiguous memory/cache locality vs pointer chasing |
| What is iterator invalidation? | Container operations can invalidate iterators/references |
| What is false sharing? | Different variables on same cache line causing coherence traffic |
| Mutex vs atomic? | Critical sections vs simple shared state; correctness first |
| What is memory ordering? | Guarantees around visibility/reordering across threads |
| What does `string_view` not own? | Underlying data lifetime must outlive the view |
| Why avoid raw owning pointers? | Leaks/double free; use RAII/smart pointers |
| How do sanitizers help? | Detect memory bugs and data races early |
| What happens on use-after-free? | Undefined behavior; may pass tests and fail in production |

## Domain 8: Performance Benchmarking and Observability

### Metrics Cheat Sheet

| Metric | Meaning | Common trap |
|---|---|---|
| IOPS | Number of I/O operations per second | Block size changes the meaning |
| Throughput | MB/s or GB/s transferred | Can be high while latency is bad |
| Average latency | Mean time per request | Hides tail latency |
| p99 latency | 99th percentile request time | Usually matters more for user experience |
| Queue depth | Outstanding operations | Too low underutilizes device; too high increases latency |
| Read/write mix | Percent reads vs writes | Writes often trigger replication, journaling, EC |
| Working set | Active data size | If it fits cache, benchmark may lie |
| Cache hit ratio | Reads served from cache | High ratio can hide slow backend |
| CPU utilization | User/sys/iowait/softirq | Storage bottlenecks can look like CPU or network |
| Network retransmits | TCP retries/loss | Small loss can produce huge p99 latency |

### Tools to Know

| Tool | Use |
|---|---|
| `fio` | Synthetic disk/storage workload generation |
| `iostat -x` | Device utilization, await, queue depth |
| `vmstat` | CPU, memory, run queue, paging |
| `pidstat` | Per-process CPU, I/O, context switching |
| `strace` | Syscall-level behavior |
| `perf` | CPU profiling and kernel hotspots |
| `bpftrace`/eBPF | Kernel tracing and live production debugging |
| `tcpdump` | Packet capture |
| `ss` | Socket state, queues, retransmits |
| Go `pprof` | CPU/heap/mutex/block profiling |
| C++ sanitizers | AddressSanitizer, ThreadSanitizer, UndefinedBehaviorSanitizer |
| gdb/lldb | Native C++ debugging |
| Prometheus/Grafana | Metrics and dashboards |

### Key Resources

| Resource | Type | Why it matters |
|---|---|---|
| [fio documentation](https://fio.readthedocs.io/en/latest/fio_doc.html) | Docs | Benchmarking storage workloads |
| [Brendan Gregg Linux performance](https://www.brendangregg.com/linuxperf.html) | Guide | Practical Linux performance methodology |
| [Brendan Gregg USE method](https://www.brendangregg.com/usemethod.html) | Guide | Utilization, saturation, errors framework |
| [Go diagnostics](https://go.dev/doc/diagnostics) | Official docs | Go production debugging |
| [LLVM AddressSanitizer](https://clang.llvm.org/docs/AddressSanitizer.html) | Tool docs | C++ memory error detection |
| [LLVM ThreadSanitizer](https://clang.llvm.org/docs/ThreadSanitizer.html) | Tool docs | C++ data race detection |
| [Linux BPF/filter docs](https://docs.kernel.org/networking/filter.html) | Official docs | BPF background |

### Benchmarking Rules

| Rule | Why |
|---|---|
| Always state block size | 4 KiB random IOPS and 1 MiB sequential throughput are different workloads |
| Warm up before measuring | First run may populate cache or trigger allocation |
| Control cache effects | Page cache can hide disk/backend performance |
| Measure p95/p99 | Tail latency is where storage systems hurt users |
| Separate read and write tests | Writes involve durability mechanisms |
| Watch CPU and network too | Storage issues often appear outside the disk |
| Do not trust one run | Repeat and compare variance |
| Avoid unrealistic queue depth | A benchmark can produce numbers no real app sees |

## Domain 9: Reliability, Data Protection, and Security

### Must Know

| Topic | What to learn |
|---|---|
| Snapshots | Point-in-time copies, crash consistency, app consistency |
| Backup | Full, incremental, synthetic full, retention, restore testing |
| Immutability | WORM, object lock, ransomware protection |
| Checksums | Detect bit rot, corruption, torn writes |
| Scrubbing | Periodic verification and repair |
| Encryption | At rest, in transit, key management, envelope encryption |
| Multi-tenancy | Tenant isolation, quotas, noisy neighbor control |
| RBAC | Least privilege, service accounts, controller permissions |
| Disaster recovery | RPO, RTO, region/AZ failover |
| Audit | Access logs, change logs, security evidence |

### Interview Scenarios

| Scenario | Strong answer includes |
|---|---|
| Ransomware encrypts production files | Immutable snapshots, versioning, detection, restore plan, least privilege |
| Disk silently corrupts data | Checksums, scrubbing, replicas/EC reconstruction |
| Backup job says success but restore fails | Restore validation, checksums, periodic drills |
| Tenant A can see Tenant B volume metadata | RBAC, namespace isolation, API authorization, audit |
| Region fails | RPO/RTO, replication mode, failover automation, consistency caveats |

## Domain 10: Job Description Mapping

Original job description link:

| Resource | Notes |
|---|---|
| [Software Engineer - Distributed Systems, Storage Protocols](https://builtin.com/job/software-engineer-distributed-systems-storage-protocols/9134652#:~:text=2%E2%80%935%20years%20of%20software,based%20environments%20(must%2Dhave)) | Use as a template for expected skills: Go, distributed systems, Linux, storage protocols, cloud/Kubernetes environments |

### How to Translate a JD Into Prep Topics

| JD keyword | Study topic | Practice task |
|---|---|---|
| Distributed systems | Replication, consensus, failure recovery | Design a distributed block/object store |
| Storage protocols | NFS, iSCSI, NVMe/TCP, RDMA | Compare protocols for DB workloads |
| Go | Concurrency, pprof, networking | Build a concurrent uploader or pipeline |
| C++ | RAII, ownership, move semantics, atomics, sanitizers | Build a buffer pool, WAL, or thread-safe cache |
| Kubernetes | PV/PVC/CSI/StatefulSet | Deploy app with persistent volume and debug binding |
| Linux | VFS, page cache, block layer | Trace file I/O with `strace`, benchmark with `fio` |
| Performance | IOPS, latency, throughput, p99 | Run fio and explain results |
| Cloud | EBS/EFS/S3, Azure/GCP equivalents | Choose storage for workload requirements |

## Hands-On Projects

### Project 1: Go Million-Record Processor

| Requirement | Details |
|---|---|
| Input | CSV/JSON lines with 1 million records |
| Processing | Validate, transform, aggregate totals |
| Constraints | Bounded memory, context cancellation, backpressure |
| Observability | CPU/heap profile with pprof |
| Interview story | Explain batching, worker pool sizing, GC pressure, profiling results |

### Project 2: Mini Object Store

| Requirement | Details |
|---|---|
| API | PUT/GET/DELETE object |
| Metadata | Object size, checksum, created time |
| Durability | Store data chunks and verify checksum |
| Extension | Add versioning or simple erasure coding simulation |
| Interview story | Explain object metadata, consistency, failure handling |

### Project 3: Linux Storage Benchmark Lab

| Step | Task |
|---|---|
| 1 | Create test files and benchmark with `fio` |
| 2 | Compare 4 KiB random read/write vs 1 MiB sequential |
| 3 | Compare buffered I/O vs direct I/O |
| 4 | Track `iostat -x`, `vmstat`, `pidstat` |
| 5 | Explain latency, throughput, queue depth, cache effects |

### Project 4: Kubernetes CSI/PVC Debug Lab

| Step | Task |
|---|---|
| 1 | Create a local kind/minikube cluster |
| 2 | Install a simple dynamic provisioner or use default storage |
| 3 | Create StorageClass, PVC, pod mount |
| 4 | Break StorageClass/provisioner settings deliberately |
| 5 | Debug with `kubectl describe`, events, controller logs |

### Project 5: NFS/iSCSI/NVMe Concept Lab

| Step | Task |
|---|---|
| 1 | Mount an NFS export locally or in Kubernetes |
| 2 | Run `fio` against NFS and local disk |
| 3 | Observe latency/throughput differences |
| 4 | Study iSCSI initiator/target concepts |
| 5 | Explain when file vs block is the right abstraction |

### Project 6: C++ Mini Write-Ahead Log and Buffer Pool

| Requirement | Details |
|---|---|
| Language | Modern C++17 or C++20 |
| WAL | Append records with length, checksum, payload |
| Durability | Support flush/fsync and crash recovery scan |
| Buffer pool | Reuse fixed-size aligned buffers instead of allocating per write |
| Concurrency | Single writer queue or mutex-protected appender |
| Debugging | Run with AddressSanitizer and ThreadSanitizer |
| Interview story | Explain RAII, ownership, partial writes, fsync, checksums, and performance tradeoffs |

## Interview Question Bank

### Linux and OS

| Question | Key points |
|---|---|
| What is an inode? | Metadata, file identity, links, directory entries |
| What happens on `fsync()`? | Dirty data/metadata flushed to durable media depending on FS/device |
| Page cache vs buffer cache? | Modern Linux page cache handles file data caching |
| `mmap()` vs `read()`? | Address mapping, page faults, copy behavior, access patterns |
| Why can free memory be low on Linux? | Page cache uses memory and can be reclaimed |
| What is `O_DIRECT`? | Bypass page cache, alignment requirements, DB use cases |
| Why is small random I/O slow? | Seek/flash translation/queue overhead, amplification |

### Storage

| Question | Key points |
|---|---|
| Explain IOPS vs throughput vs latency | Different metrics, block size and queue depth matter |
| Why can higher queue depth increase latency? | More outstanding work, device saturation, queueing delay |
| NFS vs iSCSI | File vs block, shared namespace vs raw LUN |
| NVMe/TCP vs NVMe/RoCE | Standard TCP simplicity vs RDMA latency/CPU/fabric complexity |
| Replication vs erasure coding | Speed/simplicity vs space efficiency and rebuild cost |
| Why are metadata operations expensive? | Directory/inode updates, journaling, locking, cache invalidation |
| What is a stale NFS file handle? | Server-side object changed/removed; client handle invalid |

### Distributed Systems

| Question | Key points |
|---|---|
| How do you place replicas? | Failure domains, consistent hashing/CRUSH, balance |
| How do you handle split brain? | Quorum, fencing, leader election, leases |
| How do you rebuild after disk failure? | Detect, mark out, re-replicate/EC reconstruct, throttle |
| How do snapshots work? | Copy-on-write/redirect-on-write, metadata references |
| How do you prevent hot partitions? | Hashing, sharding metadata, caching, load-aware placement |
| What is read repair? | Repair stale/corrupt replicas during reads |

### Kubernetes

| Question | Key points |
|---|---|
| What is PV vs PVC? | Cluster resource vs user claim |
| What does StorageClass do? | Provisioner, parameters, reclaim policy, binding mode |
| How does CSI work? | Controller service and node service lifecycle |
| Why is PVC pending? | No matching PV, provisioner issue, topology, access mode |
| RWO vs RWX? | Single-node writer vs multi-node read-write shared access |
| How do snapshots work in K8s? | Snapshot CRDs, driver support, consistency caveats |
| How do you secure a CSI driver? | RBAC least privilege, secrets, namespace isolation |

### Go

| Question | Key points |
|---|---|
| How do you avoid goroutine leaks? | Context cancellation, close channels, bounded queues |
| Buffered vs unbuffered channels? | Synchronization vs queueing/backpressure |
| How do you find a memory leak in Go? | Heap profile, allocation profile, object retention |
| What causes high GC pressure? | Allocation rate, object lifetimes, unnecessary copies |
| How do you make an HTTP client production-safe? | Timeouts, retries, backoff, connection pooling |
| Mutex vs channel? | Shared state vs communication; choose for clarity and correctness |

## Troubleshooting Drills

| Problem | Investigation path |
|---|---|
| Database latency spikes on cloud volume | Check p99, queue depth, IOPS limit, burst credits, CPU steal, network |
| Kubernetes pod cannot mount volume | Pod events, kubelet logs, CSI node logs, secret/RBAC, node topology |
| NFS mount is slow | Network latency, server load, client cache, rsize/wsize, sync mode, locks |
| Object uploads are slow | Multipart size, concurrency, TCP, TLS CPU, server throttling |
| Go service memory grows forever | pprof heap, goroutine dump, caches, slices retaining backing arrays |
| C++ service crashes under load | ASan/UBSan, core dump, gdb/lldb, use-after-free, data races |
| High system CPU during I/O | Kernel path, checksums, encryption, networking, context switches |
| Good average latency but bad user experience | Tail latency, retries, queueing, noisy neighbors |

## Top Bug and Incident Themes to Prepare

Storage interviews often ask about real bugs, production incidents, or "what would you check first?" scenarios. Prepare 2-3 stories where you can explain symptoms, diagnosis, root cause, fix, and prevention.

| Bug or incident theme | What to be ready to explain |
|---|---|
| Data corruption | Checksums, torn writes, bad disks, memory corruption, validation during read/repair |
| Lost or unavailable volume | Control-plane metadata, attach/detach state, node failure, quorum, recovery |
| Split brain | Leader election, fencing, quorum, stale writers, lease expiry |
| Stale NFS file handle | Server-side file/object identity changed while client kept old handle |
| Inode exhaustion | Many small files, filesystem metadata limits, monitoring beyond free bytes |
| PVC stuck in Pending | StorageClass, provisioner, topology, access mode, RBAC, events |
| Slow mount or attach | CSI node/plugin logs, kubelet, multipath, device discovery, network |
| Write latency regression | fsync path, journal, replication, EC, queue depth, network retransmits |
| Rebuild storm after failure | Throttling, prioritization, degraded reads, network/disk saturation |
| Cache inconsistency | Invalidation, leases, write-through/write-back policy, stale reads |
| Goroutine leak in Go service | Missing cancellation, blocked channel send/receive, unbounded retries |
| Memory growth in Go | Retained slices, unbounded cache, allocation rate, pprof heap profile |
| C++ memory corruption | Use-after-free, buffer overflow, invalidated references, missing ownership rules |
| C++ data race | Shared state without synchronization, incorrect atomics, lock lifetime mistakes |
| iptables/network policy issue | Dropped packets, wrong chain/order, NAT, Kubernetes NetworkPolicy, conntrack |
| Noisy neighbor | Shared disk/network saturation, cgroups, QoS, tenant quotas |

## Original Curated Links

| Topic | Link |
|---|---|
| Why Go for cloud infra | [ITFS Go cloud infrastructure article](https://itfs.com/blog/why-top-companies-choose-golang-for-cloud-infrastructure/#:~:text=Go%20approaches%20concurrency%20differently.,them%20without%20overwhelming%20your%20system.&text=Deployment%20Simplicity,management%20headaches%2C%20no%20version%20conflicts.) |
| Job description | [Software Engineer - Distributed Systems, Storage Protocols](https://builtin.com/job/software-engineer-distributed-systems-storage-protocols/9134652#:~:text=2%E2%80%935%20years%20of%20software,based%20environments%20(must%2Dhave)) |
| Go million transactions Part 1 | [Processing 1 million transactions in under a second using Go - Part 1](https://blog.karoko.dev/processing-1-million-transactions-in-under-a-second-using-go-part-1-98c9079375ac) |
| Go million transactions Part 2 | [Processing 1 million transactions in under a second using Go - Part 2](https://blog.karoko.dev/processing-1-million-transactions-in-under-a-second-using-go-part-2-c075800fe7a4) |
| Virtual memory | [Virtual Memory: page tables, TLBs, Linux internals](https://hackingcpp.com/cpp/blogs) |
| Storage networking video | [YouTube storage networking video](https://www.youtube.com/watch?v=HP3Z48VnZjk&t=1336s) |
| Ethernet storage networks | [Ethernet switches for storage networks](https://intelligentvisibility.com/ethernet-switches-for-storage-networks) |
| Kubernetes storage architecture | [Kubernetes storage architecture for databases](https://simplyblock.io/glossary/kubernetes-storage-architecture-for-databases/#questions-and-answers) |
| IOPS | [What is IOPS?](https://simplyblock.io/glossary/what-is-iops/) |
| IOPS/latency/throughput | [IOPS, throughput, latency explained](https://simplyblock.io/blog/iops-throughput-latency-explained/) |
| NVMe/TCP vs NVMe/RoCE | [Which protocol for high-performance storage?](https://simplyblock.io/blog/nvme-tcp-vs-nvme-roce/) |
| RDMA | [What is RDMA?](https://simplyblock.io/glossary/what-is-rdma/) |
| Erasure coding | [What is erasure coding?](https://simplyblock.io/blog/what-is-erasure-coding-a-shield-against-data-loss/) |
| Performance optimization | [Art of storage performance optimization](https://simplyblock.io/blog/art-of-storage-performance-optimization/) |
| NAS with Kubernetes | [NFS, SMB, and iSCSI volumes with Kubernetes](https://oneuptime.com/blog/post/2025-12-15-how-to-use-nas-storage-with-kubernetes/view) |
| Kubernetes containers | [Kubernetes containers](https://kubernetes.io/docs/concepts/containers/) |
| AI storage and Kubernetes | [AI storage unlocks Kubernetes performance](https://www.weka.io/article/your-kubernetes-workloads-aren-t-cpu-bound-they-re-waiting-on-storage) |
| Kubernetes RBAC | [RBAC good practices](https://kubernetes.io/docs/concepts/security/rbac-good-practices/) |
| Kubernetes multi-tenancy | [Multi-tenancy](https://kubernetes.io/docs/concepts/security/multi-tenancy/) |
| AI engineering repo | [ai-engineering-from-scratch](https://github.com/rohitg00/ai-engineering-from-scratch) |
| Helm | [What is Helm in Kubernetes?](https://www.sysdig.com/learn-cloud-native/what-is-helm-in-kubernetes) |
| OS textbook | [OSTEP](https://pages.cs.wisc.edu/~remzi/OSTEP/) |

## Added Resource Index

| Domain | Resources |
|---|---|
| Go | [Effective Go](https://go.dev/doc/effective_go), [Go diagnostics](https://go.dev/doc/diagnostics), [Go pipelines](https://go.dev/blog/pipelines), [Go GC guide](https://go.dev/doc/gc-guide), [Go memory model](https://go.dev/ref/mem), [Go pprof](https://go.dev/blog/pprof) |
| C++ | [cppreference](https://en.cppreference.com/w/), [C++ Core Guidelines](https://isocpp.github.io/CppCoreGuidelines/CppCoreGuidelines), [Google C++ Style Guide](https://google.github.io/styleguide/cppguide.html), [CMake tutorial](https://cmake.org/cmake/help/latest/guide/tutorial/index.html), [AddressSanitizer](https://clang.llvm.org/docs/AddressSanitizer.html), [ThreadSanitizer](https://clang.llvm.org/docs/ThreadSanitizer.html), [RocksDB wiki](https://github.com/facebook/rocksdb/wiki), [SPDK docs](https://spdk.io/doc/) |
| Linux | [Linux VFS docs](https://docs.kernel.org/filesystems/vfs.html), [Linux block docs](https://docs.kernel.org/block/index.html), [Linux IP sysctl](https://docs.kernel.org/networking/ip-sysctl.html), [netfilter docs](https://netfilter.org/documentation/), [man7](https://man7.org/linux/man-pages/) |
| Protocols | [NFSv4 RFC 7530](https://www.rfc-editor.org/info/rfc7530/), [iSCSI RFC 7143](https://datatracker.ietf.org/doc/html/rfc7143), [NVM Express specs](https://nvmexpress.org/specifications/), [NVIDIA RDMA manual](https://networking-docs.nvidia.com/rdmaawareprogramming) |
| Distributed storage | [Ceph architecture](https://docs.ceph.com/en/latest/architecture/), [MinIO erasure coding](https://docs.min.io/aistor/operations/core-concepts/erasure-coding/), [GFS paper](https://research.google/pubs/the-google-file-system/), [Dynamo paper](https://www.allthingsdistributed.com/files/amazon-dynamo-sosp2007.pdf), [Raft paper](https://raft.github.io/raft.pdf) |
| Kubernetes | [Volumes](https://kubernetes.io/docs/concepts/storage/volumes/), [PersistentVolumes](https://kubernetes.io/docs/concepts/storage/persistent-volumes/), [StorageClasses](https://kubernetes.io/docs/concepts/storage/storage-classes/), [Dynamic provisioning](https://kubernetes.io/docs/concepts/storage/dynamic-provisioning/), [Snapshots](https://kubernetes.io/docs/concepts/storage/volume-snapshots/), [CSI spec](https://github.com/container-storage-interface/spec/blob/master/spec.md), [Using Helm](https://helm.sh/docs/intro/using_helm/) |
| Cloud storage | [AWS EBS](https://docs.aws.amazon.com/ebs/latest/userguide/what-is-ebs.html), [AWS S3](https://docs.aws.amazon.com/AmazonS3/latest/userguide/Welcome.html), [AWS EFS](https://docs.aws.amazon.com/efs/latest/ug/whatisefs.html), [Google Cloud storage advisor](https://docs.cloud.google.com/architecture/storage-advisor), [Azure Storage](https://learn.microsoft.com/en-us/azure/storage/common/storage-introduction), [Azure managed disks](https://learn.microsoft.com/en-us/azure/virtual-machines/managed-disks-overview) |
| Performance | [fio docs](https://fio.readthedocs.io/en/latest/fio_doc.html), [Brendan Gregg Linux performance](https://www.brendangregg.com/linuxperf.html), [USE method](https://www.brendangregg.com/usemethod.html) |

## Final Prep Checklist

| Done | Item |
|---|---|
| [ ] | Explain block vs file vs object storage with examples |
| [ ] | Explain IOPS, throughput, latency, p99, queue depth |
| [ ] | Explain Linux page cache, inode, VFS, and `fsync()` |
| [ ] | Compare NFS, iSCSI, NVMe/TCP, NVMe/RoCE, S3 |
| [ ] | Design a distributed object store |
| [ ] | Explain replication vs erasure coding |
| [ ] | Debug a PVC stuck in `Pending` |
| [ ] | Explain CSI controller vs node plugin |
| [ ] | Build one Go concurrency project and profile it |
| [ ] | Build one C++ systems project with RAII, buffers, and sanitizer runs |
| [ ] | Explain C++ ownership, move semantics, atomics, and memory-layout tradeoffs |
| [ ] | Run `fio` and explain the results |
| [ ] | Prepare two real debugging stories from past work/projects |
| [ ] | Prepare one storage architecture deep dive for interviews |
