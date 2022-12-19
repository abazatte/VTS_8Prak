package de.hsos.vs;

import de.hsos.vs.BillBoardHtmlAdapter;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(asyncSupported = true, urlPatterns = {"/BillBoardServer"})
public class BillBoardServlet extends HttpServlet {
    private final BillBoardHtmlAdapter bb = new BillBoardHtmlAdapter("BillBoardServer");
    Map<String, Long> time = new HashMap<>();

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String caller_ip = request.getRemoteAddr();
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        System.out.println("BillBoardServer - GET (" + caller_ip + "): full output");
        String answer = "";
        answer = request.getParameter("rtype");
        answer = bb.readEntries(caller_ip);
        try {
            out.println(answer);
        } finally {
            out.close();
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String caller_ip = request.getRemoteAddr();

        JSONObject jsonObjectM;
        String content = "";
        System.out.println("BillBoardServer - POST (" + caller_ip + ")");
        response.setContentType("text/html;charset=UTF-8");
        StringBuilder builder = new StringBuilder();
        BufferedReader buff = request.getReader();
        try {
            String line;
            while ((line = buff.readLine()) != null) {
                builder.append(line).append('\n');
                System.out.println("JSON: " + builder);
            }
        } finally {
            buff.close();
        }

        // create JSON object with value "input" from JSON object
        try {
            jsonObjectM = new JSONObject(builder.toString());
            content = jsonObjectM.getString("input");
        } catch (JSONException ex) {
            System.out.println("Exception: JSONException: doPost()");
        }
        response.getWriter().close();
        bb.createEntry(content, caller_ip);   //dem Billboard einen Eintrag hinzufügen
    }

    /**
     * Handles the HTTP <code>DELETE</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String caller_ip = request.getRemoteAddr();
        System.out.println("BillBoardServer - DELETE (" + caller_ip + ")");

        JSONObject jsonObjectM;
        int id = 0;
        StringBuilder builder = new StringBuilder();
        BufferedReader buff = request.getReader();
        try {
            String line;
            while ((line = buff.readLine()) != null) {
                builder.append(line).append('\n');
                System.out.println("JSON: " + builder);
            }
        } finally {
            buff.close();
        }

        // create JSON object with value "id" from JSON object
        try {
            jsonObjectM = new JSONObject(builder.toString());
            id = Integer.parseInt(jsonObjectM.getString("id"));
        } catch (JSONException ex) {
            System.out.println("Exception: JSONException: doDelete()");
        }

        System.out.println("Deleting ID: " + id);

        bb.deleteEntry(id);   //dem Billboard einen Eintrag hinzufügen
    }

    /**
     * Handles the HTTP <code>PUT</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String caller_ip = request.getRemoteAddr();
        System.out.println("BillBoardServer - PUT (" + caller_ip + ")");

        JSONObject jsonObjectM;
        StringBuilder builder = new StringBuilder();
        BufferedReader requestReader = request.getReader();
        String text = "";
        int idx = 0;
        try {
            String line;
            while ((line = requestReader.readLine()) != null) {
                builder.append(line).append('\n');
                System.out.println("JSON: " + builder);
            }
        } finally {
            requestReader.close();
        }
        try {
            jsonObjectM = new JSONObject(builder.toString());
            text = jsonObjectM.getString("text");
            idx = Integer.parseInt(jsonObjectM.getString("id"));
        } catch (JSONException ex) {
            System.out.println("Exception: JSONException: doPut()");
        }
        bb.updateEntry(idx, text, caller_ip);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "BillBoard Servlet";
    }// </editor-fold>
}


/*package de.hsos.vs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Implementierung des BillBoard-Servers.
 * In dieser Version unterstützt er asynchrone Aufrufe.
 * Damit wird die Implementierung von Long Polling möglich:
 * Anfragen (HTTP GET) werden nicht sofort wie bei zyklischem
 * Polling beantwortet sondern verbleiben so lange im System,
 * bis eine Änderung an den Client gemeldet werden kann.
 *
 * @author heikerli

@WebServlet(asyncSupported = true, urlPatterns = {"/BillBoardServer"})
public class BillBoardServlet extends HttpServlet {
    private final BillBoardHtmlAdapter bb = new BillBoardHtmlAdapter("BillBoardServer");
    private List<HttpSession> sessions = new ArrayList<>();
    private int currentlyWaiting = 0;

    synchronized public void waitForUpdate(HttpSession session) {
        if (!(Boolean) session.getAttribute("upToDate")) {
            return;
        }
        try {
            wait();
        } catch (InterruptedException e) {
            System.err.println("<info> wait wurde abgebrochen");
        }
    }

    synchronized public void anounceUpdate() {
        this.sessions.forEach(session -> session.setAttribute("upToDate", false));
        notifyAll();
        this.currentlyWaiting = 0;
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
//        AsyncContext ac = request.startAsync();
        String caller_ip = request.getRemoteAddr();
        String firstGet = request.getParameter("firstGet");
        System.out.println("<info> - GET (" + caller_ip + "): full output + first get: " + firstGet);
        if(Boolean.parseBoolean(firstGet)){
            request.getSession(false).setAttribute("upToDate", false);
        }
        PrintWriter out = response.getWriter();
        if (request.getSession(false).getAttribute("name") == null) {
            response.setContentType("text/html;charset=UTF-8");
            HttpSession session = request.getSession(true);
            session.setAttribute("name", caller_ip);
            session.setAttribute("upToDate", true);
            this.sessions.add(session);
            System.out.println("<info> " + caller_ip + " hat sich angemeldet");
        } else {
            if ((Boolean) request.getSession(false).getAttribute("upToDate")) {
                this.currentlyWaiting++;
                System.out.println("<info> " + this.currentlyWaiting + " Client/en warten");
                this.waitForUpdate(request.getSession(false));
                System.out.println("<info> client holt sich das update");
            }
        }
        String table = bb.convertToJson(caller_ip);
        request.getSession(false).setAttribute("upToDate", true);
        try {
            response.setContentType("application/json;charset=UTF-8");
            out.println(table);
        } finally {
            out.close();
        }
//        ac.complete();
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String caller_ip = request.getRemoteAddr();
        String eintrag = request.getParameter("name");
        System.out.println("<info> - POST (" + caller_ip + ") + Eintrag: " + eintrag);
        if (eintrag != null) {
            if (!eintrag.isEmpty()) {
                this.bb.createEntry(eintrag, caller_ip);
                System.out.println("<info> neuer Eintrag: " + eintrag);
                this.anounceUpdate();
            }
        }
    }

    /**
     * Handles the HTTP <code>DELETE</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String caller_ip = request.getRemoteAddr();
        String entryID = request.getParameter("id");
        System.out.println("<info> - DELETE (" + caller_ip + ") EintragID: " + entryID);
        String[] params = getParamsFromPost(request);
        if (params.length == 1) {
            System.out.println("<info> Eintrag " + params[0] + " wurde geloescht");
            bb.deleteEntry(Integer.parseInt(params[0]));
            this.anounceUpdate();
        }
    }

    /**
     * Handles the HTTP <code>PUT</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String caller_ip = request.getRemoteAddr();
        String entryID = request.getParameter("id");
        String plakatName = request.getParameter("name");
        System.out.println("<info> - PUT (" + caller_ip + ") + EintragID: " + entryID + " Inhalt:" + plakatName);
        String[] params = getParamsFromPost(request);
        if (params.length == 2) {
            this.bb.updateEntry(Integer.parseInt(params[0]), params[1], caller_ip);
            System.out.println("<info> Eintrag( " + params[0] + "): wurde mit " + params[1] + " ueberschrieben");
            this.anounceUpdate();
        }
    }

    private String[] getParamsFromPost(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line = reader.readLine();
        reader.close();
        String[] rawParams = line.split("&");
        String[] cleanParams = new String[rawParams.length];
        for (int i = 0; i < rawParams.length; i++) {
            cleanParams[i] = rawParams[i].split("=")[1];
            System.out.println(cleanParams[i]);
        }
        return cleanParams;
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description

    @Override
    public String getServletInfo() {
        return "BillBoard Servlet";
    }
}
*/