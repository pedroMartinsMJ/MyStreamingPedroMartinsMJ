package pedroMartinsMJ.MyStreaming.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileSystemService — Testes de sistema de arquivos")
class FileSystemServiceTest {

    @TempDir
    Path tempDir;

    private FileSystemService fileSystemService;

    @BeforeEach
    void setUp() throws Exception {
        // Cria subdiretórios para simular o ambiente real
        Files.createDirectories(tempDir.resolve("videos"));
        Files.createDirectories(tempDir.resolve("encoded"));
        Files.createDirectories(tempDir.resolve("thumbnails"));
        Files.createDirectories(tempDir.resolve("temp"));

        fileSystemService = new FileSystemService();

        // Injeta os paths via reflection (os @Value não funcionam em teste unitário puro)
        java.lang.reflect.Field videoPathField = FileSystemService.class.getDeclaredField("videoStoragePath");
        videoPathField.setAccessible(true);
        videoPathField.set(fileSystemService, tempDir.resolve("videos").toString());

        java.lang.reflect.Field encodedPathField = FileSystemService.class.getDeclaredField("encodedStoragePath");
        encodedPathField.setAccessible(true);
        encodedPathField.set(fileSystemService, tempDir.resolve("encoded").toString());

        java.lang.reflect.Field thumbPathField = FileSystemService.class.getDeclaredField("thumbnailStoragePath");
        thumbPathField.setAccessible(true);
        thumbPathField.set(fileSystemService, tempDir.resolve("thumbnails").toString());

        java.lang.reflect.Field tempPathField = FileSystemService.class.getDeclaredField("tempPath");
        tempPathField.setAccessible(true);
        tempPathField.set(fileSystemService, tempDir.resolve("temp").toString());
    }

    // ==================== PATH GETTERS ====================

    @Nested
    @DisplayName("get*StoragePath()")
    class PathGetterTests {

        @Test
        @DisplayName("Deve retornar video storage path e criar diretório se necessário")
        void shouldReturnVideoStoragePathAndCreateDir() {
            String path = fileSystemService.getVideoStoragePath();
            assertNotNull(path);
            assertTrue(new File(path).exists());
        }

        @Test
        @DisplayName("Deve retornar encoded storage path e criar diretório se necessário")
        void shouldReturnEncodedStoragePathAndCreateDir() {
            String path = fileSystemService.getEncodedStoragePath();
            assertNotNull(path);
            assertTrue(new File(path).exists());
        }

        @Test
        @DisplayName("Deve retornar thumbnail storage path e criar diretório se necessário")
        void shouldReturnThumbnailStoragePathAndCreateDir() {
            String path = fileSystemService.getThumbnailStoragePath();
            assertNotNull(path);
            assertTrue(new File(path).exists());
        }

        @Test
        @DisplayName("Deve retornar temp path e criar diretório se necessário")
        void shouldReturnTempPathAndCreateDir() {
            String path = fileSystemService.getTempPath();
            assertNotNull(path);
            assertTrue(new File(path).exists());
        }
    }

    // ==================== FILE OPERATIONS ====================

    @Nested
    @DisplayName("fileExists(), getFileSize(), getFile()")
    class FileOperationTests {

        private Path testFile;

        @BeforeEach
        void createTestFile() throws IOException {
            testFile = tempDir.resolve("test.mp4");
            Files.write(testFile, "dummy video content".getBytes());
        }

        @Test
        @DisplayName("Deve retornar true para arquivo existente")
        void shouldReturnTrueForExistingFile() {
            assertTrue(fileSystemService.fileExists(testFile.toString()));
        }

        @Test
        @DisplayName("Deve retornar false para arquivo inexistente")
        void shouldReturnFalseForNonExistentFile() {
            assertFalse(fileSystemService.fileExists(tempDir.resolve("nonexistent.mp4").toString()));
        }

        @Test
        @DisplayName("Deve retornar tamanho correto do arquivo")
        void shouldReturnCorrectFileSize() {
            long size = fileSystemService.getFileSize(testFile.toString());
            assertEquals(19, size); // "dummy video content".length()
        }

        @Test
        @DisplayName("Deve retornar File object para caminho válido")
        void shouldReturnFileForValidPath() {
            File file = fileSystemService.getFile(testFile.toString());
            assertNotNull(file);
            assertTrue(file.exists());
        }
    }

    // ==================== DISK SPACE ====================

    @Nested
    @DisplayName("getAvailableDiskSpace() e getDirectorySize()")
    class DiskSpaceTests {

        @Test
        @DisplayName("Deve retornar espaço em disco disponível (valor > 0)")
        void shouldReturnPositiveDiskSpace() {
            long space = fileSystemService.getAvailableDiskSpace();
            assertTrue(space > 0, "Espaço em disco deve ser maior que 0");
        }

        @Test
        @DisplayName("Deve retornar tamanho do diretório corretamente")
        void shouldReturnCorrectDirectorySize() throws IOException {
            // Cria arquivos no diretório de vídeos
            Path videoDir = tempDir.resolve("videos");
            Files.write(videoDir.resolve("a.mp4"), "1234567890".getBytes()); // 10 bytes
            Files.write(videoDir.resolve("b.mp4"), "12345678901234567890".getBytes()); // 20 bytes

            long size = fileSystemService.getDirectorySize(videoDir.toString());
            assertEquals(30, size);
        }

        @Test
        @DisplayName("Deve retornar 0 para diretório inexistente")
        void shouldReturnZeroForNonExistentDirectory() throws IOException {
            long size = fileSystemService.getDirectorySize(tempDir.resolve("nonexistent").toString());
            assertEquals(0, size);
        }

        @Test
        @DisplayName("Deve calcular tamanho de diretório recursivamente")
        void shouldCalculateRecursiveDirectorySize() throws IOException {
            Path videoDir = tempDir.resolve("videos");
            Files.write(videoDir.resolve("a.mp4"), "1234567890".getBytes()); // 10 bytes

            // Subdiretório com arquivo
            Path subDir = Files.createDirectory(videoDir.resolve("subdir"));
            Files.write(subDir.resolve("b.mp4"), "12345678901234567890".getBytes()); // 20 bytes

            long size = fileSystemService.getDirectorySize(videoDir.toString());
            assertEquals(30, size);
        }
    }

    // ==================== MEDIA FILE DETECTION ====================

    @Nested
    @DisplayName("isValidMediaFile() e findMediaFiles()")
    class MediaDetectionTests {

        @Test
        @DisplayName("Deve retornar true para arquivo de vídeo válido (.mp4)")
        void shouldReturnTrueForMp4File() throws IOException {
            File file = tempDir.resolve("video.mp4").toFile();
            Files.write(file.toPath(), "content".getBytes());
            assertTrue(fileSystemService.isValidMediaFile(file));
        }

        @Test
        @DisplayName("Deve retornar true para arquivo de vídeo válido (.mkv)")
        void shouldReturnTrueForMkvFile() throws IOException {
            File file = tempDir.resolve("video.mkv").toFile();
            Files.write(file.toPath(), "content".getBytes());
            assertTrue(fileSystemService.isValidMediaFile(file));
        }

        @Test
        @DisplayName("Deve retornar true para arquivo de vídeo válido (.avi)")
        void shouldReturnTrueForAviFile() throws IOException {
            File file = tempDir.resolve("video.avi").toFile();
            Files.write(file.toPath(), "content".getBytes());
            assertTrue(fileSystemService.isValidMediaFile(file));
        }

        @Test
        @DisplayName("Deve retornar false para arquivo não-mídia (.txt)")
        void shouldReturnFalseForNonMediaFile() throws IOException {
            File file = tempDir.resolve("readme.txt").toFile();
            Files.write(file.toPath(), "content".getBytes());
            assertFalse(fileSystemService.isValidMediaFile(file));
        }

        @Test
        @DisplayName("Deve retornar false para diretório")
        void shouldReturnFalseForDirectory() {
            File dir = tempDir.resolve("subdir").toFile();
            dir.mkdirs();
            assertFalse(fileSystemService.isValidMediaFile(dir));
        }

        @Test
        @DisplayName("Deve encontrar arquivos de mídia recursivamente")
        void shouldFindMediaFilesRecursively() throws IOException {
            Path videoDir = tempDir.resolve("scan_test");
            Files.createDirectories(videoDir.resolve("subdir"));

            // Cria arquivos de vídeo e não-vídeo
            Files.write(videoDir.resolve("a.mp4"), "content".getBytes());
            Files.write(videoDir.resolve("b.txt"), "not media".getBytes());
            Files.write(videoDir.resolve("subdir/c.mkv"), "content".getBytes());

            List<File> result = fileSystemService.findMediaFiles(videoDir.toString());

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Deve retornar lista vazia para diretório inexistente")
        void shouldReturnEmptyListForNonExistentDirectory() {
            List<File> result = fileSystemService.findMediaFiles(tempDir.resolve("nonexistent").toString());
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar lista vazia para diretório vazio")
        void shouldReturnEmptyListForEmptyDirectory() throws IOException {
            Path emptyDir = tempDir.resolve("empty");
            Files.createDirectory(emptyDir);

            List<File> result = fileSystemService.findMediaFiles(emptyDir.toString());
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ==================== DELETE FILE ====================

    @Nested
    @DisplayName("deleteFile()")
    class DeleteFileTests {

        private Path testFile;

        @BeforeEach
        void createTestFile() throws IOException {
            testFile = tempDir.resolve("to_delete.mp4");
            Files.write(testFile, "content".getBytes());
        }

        @Test
        @DisplayName("Deve deletar arquivo existente")
        void shouldDeleteExistingFile() throws IOException {
            assertTrue(Files.exists(testFile));

            fileSystemService.deleteFile(testFile.toString());

            assertFalse(Files.exists(testFile));
        }

        @Test
        @DisplayName("Não deve lançar exceção para arquivo inexistente")
        void shouldNotThrowForNonExistentFile() {
            try {
                fileSystemService.deleteFile(tempDir.resolve("nonexistent.mp4").toString());
            } catch (IOException e) {
                fail("deleteFile não deveria lançar IOException para arquivo inexistente: " + e.getMessage());
            }
        }
    }
}