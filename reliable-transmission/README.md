# Implementing Reliable Data Tansfer Over an Unreliable Link
Implementing Go-Back-N (NACKless) algorithm over unreliable UDP to ensure reliable transmission between sender and receiver.
Packets are created with a random payload and are corrupted and lost with adjustable rates.
Sender and Receiver use checksum style algorithm to validate packets.