import gemini.Gemini;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/GerarRespostaServlet")
public class GerarRespostaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Recupera os dados do formulário
        String nome = request.getParameter("nome");
        String email = request.getParameter("email");
        String mensagem = request.getParameter("mensagem");

        String respostaGemini = "";
        try {
            // Envia a mensagem para o Gemini e obtém a resposta
            respostaGemini = Gemini.getCompletion(mensagem);
        } catch (Exception e) {
            respostaGemini = "Erro ao obter resposta do Gemini: " + e.getMessage();
        }

        // Configura a resposta para download do PDF
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"resposta.pdf\"");

        // Cria o documento PDF
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.setLeading(20f);
                contentStream.newLineAtOffset(50, 700);

                // Adiciona os dados do formulário ao PDF
                contentStream.showText("Dados do Usuário");
                contentStream.newLine();
                contentStream.newLineAtOffset(0, -20); // Ajuste de posição para uma nova linha

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText("Nome: " + nome);
                contentStream.newLine();
                contentStream.showText("Email: " + email);
                contentStream.newLine();
                contentStream.showText("Mensagem: " + mensagem);
                contentStream.newLine();

                // Adiciona a resposta do Gemini ao PDF
                contentStream.newLineAtOffset(0, -40); // Ajusta a posição
                contentStream.showText("Resposta do Gemini:");
                contentStream.newLine();
                contentStream.showText(respostaGemini);

                contentStream.endText();
            }

            // Salva o PDF na resposta do Servlet
            document.save(response.getOutputStream());
        } catch (IOException e) {
            throw new ServletException("Erro ao gerar o PDF: " + e.getMessage());
        }
    }
}
