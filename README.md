This project is an intelligent collaborative system for factory manufacturing in an edge computing environment, aimed at solving the difficulties of collaboration and high costs in factory manufacturing. It helps businesses to allocate resources reasonably and reduce costs. Using this system, the entire factory's cloud-edge system can be interconnected. In the cloud, one can view the status of edge computing devices and cloud, as well as information about ongoing tasks, task completion rates, and other data. It also calculates estimated completion times, resource utilization rates, and usage efficiency, displaying these in visual chart formats. Additionally, the system provides two user interfaces for both automated and manual resource allocation. It also includes modules for messaging, security control interactions, data exchanges, and logging.

**Structure**

![image](https://github.com/ToroshiBenitobi/TermiteFactory/assets/82752385/58502dca-3243-4779-b807-b73a8ea91ea5)

> **TermiteFactory:** Run on the main server.
> - **Scheduling:** Scheduling algorithm based on the GWO algorithm and jMetal library, run by request.
> - **TermiteFactory:** Backend of the main server.
>   - **MoquetteServer:** Server for communication between the main server and devices.
>   - **SpringApplication:** Backend of the user interface.

> **TermiteClient:** Run on the edge device.
> - **MqttClient:** Client for communication between the main server and devices.
> - **SpringApplication:** Backend of the user interface for the edge device.
>
> ***Front End***: based on Vue.js, not included in the repo.


Interface Display:

![image](https://github.com/ToroshiBenitobi/TermiteFactory/assets/82752385/8427474a-9a37-44ac-963b-9369d8ee2cc0)

Device Management:

![image](https://github.com/ToroshiBenitobi/TermiteFactory/assets/82752385/7839e5db-a0cb-4eac-847e-f40c6200434d)

Console Allocation (Manual Allocation):

![image](https://github.com/ToroshiBenitobi/TermiteFactory/assets/82752385/46e83cd3-92f2-4e5f-b2a5-28751b828dc3)

Console Allocation (Automatic Allocation):

![image](https://github.com/ToroshiBenitobi/TermiteFactory/assets/82752385/01669d99-c5d2-4e84-bc2e-ad971a32181a)

Logs:

![image](https://github.com/ToroshiBenitobi/TermiteFactory/assets/82752385/8473739a-ecdf-401d-aeac-785ace365087)

Event Management:

![image](https://github.com/ToroshiBenitobi/TermiteFactory/assets/82752385/578af556-669b-409d-a914-26205efb0727)

