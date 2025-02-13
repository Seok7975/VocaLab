-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: mysql.21v.in    Database: VocaLab
-- ------------------------------------------------------
-- Server version	9.1.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `Files`
--

DROP TABLE IF EXISTS `Files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Files` (
  `fileId` int NOT NULL AUTO_INCREMENT,
  `userId` varchar(100) NOT NULL,
  `tableId` int DEFAULT NULL,
  `category` enum('NONE','COMPILE','TEST','ESSAY','BOARD','PROFILE','WORD','TESTRECORD') NOT NULL DEFAULT 'NONE',
  `fileType` enum('FILE','IMAGE') NOT NULL DEFAULT 'FILE',
  `filePath` varchar(255) NOT NULL,
  `uploadedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`fileId`),
  UNIQUE KEY `filePath` (`filePath`),
  KEY `userId` (`userId`),
  CONSTRAINT `Files_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `Users` (`userId`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=85 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Files`
--

LOCK TABLES `Files` WRITE;
/*!40000 ALTER TABLE `Files` DISABLE KEYS */;
INSERT INTO `Files` VALUES (5,'testAdmin',28,'BOARD','IMAGE','/images/upload/board/633f3114-cb56-4cdb-a491-39a92b8b36cf.png','2025-01-09 02:34:14'),(6,'testAdmin',28,'BOARD','IMAGE','/images/upload/board/fed394d9-8d4a-4b03-9d10-a108c920e82f.png','2025-01-09 02:34:14'),(7,'testAdmin',29,'BOARD','IMAGE','/images/upload/board/235ab774-fdc5-4edd-a403-e350e58544d3.jpg','2025-01-09 02:34:31'),(9,'testAdmin',30,'BOARD','IMAGE','/images/upload/board/516034d2-f351-41de-8160-5444e0db5dc2.png','2025-01-09 02:51:27'),(10,'testAdmin',31,'BOARD','IMAGE','/images/upload/board/97d8f100-8f76-460b-86f9-9cf945001d1f.jpg','2025-01-09 02:54:15'),(11,'testAdmin',32,'BOARD','IMAGE','/images/upload/board/01dca90d-bebd-47ba-b5b1-bdf78800a7f2.jpg','2025-01-09 04:09:56'),(12,'testAdmin',33,'BOARD','IMAGE','/images/upload/board/33/373b4a6a-b304-4355-b7e8-2de46dad9ab7.jpg','2025-01-09 04:15:09'),(13,'testAdmin',34,'BOARD','IMAGE','/images/upload/f75c65d9-dc49-4bd2-b446-d1ad12839d17.jpg','2025-01-09 04:16:56'),(14,'testAdmin',35,'BOARD','IMAGE','/images/upload/board/ddd9e742-1193-4049-9b40-69700cdfb99b.png','2025-01-09 05:07:57'),(48,'testAdmin',42,'BOARD','IMAGE','/images/upload/board/3f874d99-b8c2-41ed-81f5-0762ba1cb0f8.png','2025-01-10 07:35:05'),(55,'testAdmin',56,'BOARD','IMAGE','/images/upload/board/56/e077f6a0-383d-4f15-91ee-51a004aa0e41.png','2025-01-11 08:31:05'),(59,'testAdmin',57,'BOARD','IMAGE','/images/upload/board/57/187d5965-5777-46d5-b660-cffda1cf43f4.png','2025-01-11 08:39:04'),(60,'testAdmin',57,'BOARD','IMAGE','/images/upload/board/57/fdba40e7-312e-4c50-857a-ea0c42759b30.webp','2025-01-11 08:39:04'),(61,'testAdmin',58,'BOARD','IMAGE','/images/upload/board/58/219a1d52-c620-456c-bed4-4907720994d4.png','2025-01-11 08:39:31'),(62,'testAdmin',58,'BOARD','IMAGE','/images/upload/board/58/69413fa6-f505-4c00-99f2-519f1c715f39.png','2025-01-11 08:39:31'),(63,'testAdmin',58,'BOARD','IMAGE','/images/upload/board/58/a8af439c-818f-4c23-b0f7-6b93b6258aeb.png','2025-01-11 08:39:31'),(64,'testAdmin',63,'BOARD','IMAGE','/images/upload/board/63/8db7aa61-66e6-40d1-89a1-8a9908042f35.png','2025-01-11 09:51:20'),(66,'testAdmin',66,'BOARD','IMAGE','/images/upload/board/66/671f3073-68c0-498e-b49a-9f562f942ced.webp','2025-01-11 09:52:33'),(67,'testAdmin',67,'BOARD','IMAGE','/images/upload/board/67/0aa38189-8495-4cd4-8524-1d887d4268e0.png','2025-01-11 09:56:10'),(68,'testAdmin',67,'BOARD','IMAGE','/images/upload/board/67/686328f9-aeac-4d2e-a0dc-3baae7071887.png','2025-01-11 09:56:10'),(69,'testAdmin',67,'BOARD','IMAGE','/images/upload/board/67/a07b6d49-8491-4478-af83-e849fa7337b6.png','2025-01-11 09:56:10'),(70,'testAdmin',67,'BOARD','IMAGE','/images/upload/board/67/c0f64b49-fceb-463d-bda2-bd16fec014ec.png','2025-01-11 09:56:10'),(71,'testAdmin',67,'BOARD','IMAGE','/images/upload/board/67/d1f44726-dadb-442c-9009-e8ab404d096b.png','2025-01-11 09:56:10'),(72,'testAdmin',68,'BOARD','IMAGE','/images/upload/board/68/da045b31-44f9-47bb-89ad-55b59bc229ff.png','2025-01-11 10:50:04'),(78,'testAdmin',72,'BOARD','IMAGE','/images/upload/board/72/b8c7a7af-e10b-42e8-a94d-6a72f3167140.gif','2025-01-13 00:58:34'),(80,'testAdmin',74,'BOARD','IMAGE','/images/upload/board/74/bd6dec2b-55e5-4037-a399-c47f6faf93ef.png','2025-01-13 01:11:11'),(81,'testAdmin',47,'BOARD','IMAGE','/images/upload/board/47/fe9d95aa-3a25-48ea-8633-bc6e337bead4.png','2025-01-13 01:49:38'),(82,'testAdmin',75,'BOARD','IMAGE','/images/upload/board/75/914995ca-7f08-440a-9263-b0ef2cad3c35.jpg','2025-01-13 01:56:08'),(83,'testAdmin',76,'BOARD','IMAGE','/images/upload/board/76/ca19392f-5904-466f-b968-25a2ac272910.jpg','2025-01-13 03:34:30'),(84,'testAdmin',61,'BOARD','IMAGE','/images/upload/board/61/40463e35-8ab3-48db-9549-089f01b0f8cd.jpg','2025-01-13 03:36:43');
/*!40000 ALTER TABLE `Files` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-01-14 17:08:25
