package com.valeriesantos;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class EmailController {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailOrigen;

    @Value("${app.manager.nombre}")
    private String managerNombre;

    @Value("${app.manager.contacto}")
    private String managerContacto;

    public EmailController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostMapping("/enviar")
    public CompletableFuture<ResponseEntity<Map<String, String>>> enviar(@RequestBody Map<String, String> body) {
        String nombre = body.getOrDefault("nombre", "").trim();
        String email  = body.getOrDefault("email", "").trim();
        String evento = body.getOrDefault("evento", "").trim();
        String fecha  = body.getOrDefault("fecha", "").trim();
        String lang   = body.getOrDefault("lang", "es").trim();

        if (nombre.isEmpty() || email.isEmpty()) {
            String err = lang.equals("en") ? "Name and email are required." : "Nombre y email son obligatorios.";
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(Map.of("error", err)));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                String asunto;
                String cuerpo;

                if (lang.equals("en")) {
                    String eventoTexto = evento.isEmpty() ? "what you do" : "at " + evento;
                    String cierreTexto = fecha.isEmpty()
                        ? "If you think she could be a good fit and have any dates available, feel free to reach out — no pressure at all."
                        : "If you think she could be a good fit and have any dates available around " + fecha + ", feel free to reach out — no pressure at all.";

                    asunto = "Proposal for " + (evento.isEmpty() ? "your events" : evento) + " – Valerie Santos";
                    cuerpo = "Hey " + nombre + "! 👋\n\n"
                        + "I'm reaching out because I think Valerie Santos could be a great match for " + eventoTexto + " — and it would be amazing to work together.\n\n"
                        + "Valerie is a DJ and producer specialising in hard techno and schranz, with a presence on the national and international underground scene. "
                        + "Raw sound, heavy groove and the kind of energy in the booth that gets the floor moving from the very first track.\n\n"
                        + "You can check out her work here:\n\n"
                        + "📲 Instagram: @valeriesantosdj\n"
                        + "🎧 SoundCloud: soundcloud.com/dj-valerie-santos\n\n"
                        + cierreTexto + "\n\n"
                        + "Cheers,\n"
                        + managerNombre + "\n"
                        + managerContacto;
                } else {
                    String eventoTexto = evento.isEmpty() ? "con lo que hacéis" : "en " + evento;
                    String cierreTexto = fecha.isEmpty()
                        ? "Si os encaja el perfil y tenéis alguna fecha disponible, podemos hablarlo sin compromiso."
                        : "Si os encaja el perfil y tenéis alguna fecha disponible para " + fecha + ", podemos hablarlo sin compromiso.";

                    asunto = "Propuesta para " + (evento.isEmpty() ? "vuestros eventos" : evento) + " – Valerie Santos";
                    cuerpo = "¡Hola " + nombre + "! 👋\n\n"
                        + "Os escribo porque creo que Valerie Santos podría encajar muy bien " + eventoTexto + " y sería genial poder colaborar juntos.\n\n"
                        + "Valerie es DJ y productora de hard techno y schranz con presencia en la escena underground nacional e internacional. "
                        + "Sonido crudo, mucho groove y una energía en cabina que no deja a nadie parado.\n\n"
                        + "Aquí podéis echarle un vistazo a su trabajo:\n\n"
                        + "📲 Instagram: @valeriesantosdj\n"
                        + "🎧 SoundCloud: soundcloud.com/dj-valerie-santos\n\n"
                        + cierreTexto + "\n\n"
                        + "Un abrazo,\n"
                        + managerNombre + "\n"
                        + managerContacto;
                }

                MimeMessage msg = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
                helper.setFrom(emailOrigen, managerNombre);
                helper.setTo(email);
                helper.setSubject(asunto);
                helper.setText(cuerpo);
                mailSender.send(msg);

                String ok = lang.equals("en") ? "Email sent successfully to " + email : "Email enviado correctamente a " + email;
                return ResponseEntity.ok(Map.of("ok", ok));

            } catch (Exception e) {
                String err = lang.equals("en") ? "Error sending email: " + e.getMessage() : "Error al enviar: " + e.getMessage();
                return ResponseEntity.<Map<String, String>>internalServerError().body(Map.of("error", err));
            }
        });
    }
}
