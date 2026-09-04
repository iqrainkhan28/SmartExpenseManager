import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.sql.Date;

public class SmartExpenseManagerJDBC1 extends JFrame {

    // ================= DB =================
    private static final String DB_URL="jdbc:oracle:thin:@localhost:1521:XE";
    private static final String DB_USER="system";
    private static final String DB_PASS="system";

    private Connection con() throws SQLException {
        return DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
    }

    // ================= MODEL =================
    static class Expense{
        int id; LocalDate d; String c,t,n; double a;
        static final DateTimeFormatter FMT =
                DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Expense(int i,LocalDate d,String c,String t,double a,String n){
            id=i;this.d=d;this.c=c;this.t=t;this.a=a;this.n=n;
        }
    }

    // ================= UI =================
    JTextField tfDate,tfTitle,tfAmount,tfNote,tfSearch;
    JComboBox<String> cbCat;
    JTable table;
    DefaultTableModel model;
    JLabel lblTotal,lblBudget,lblRemain;
    double budget=0;
    ChartPanel chart;

    String[] CATS={"Food","Travel","Shopping","Bills",
            "Entertainment","Health","Other"};

    java.util.List<Expense> data=new ArrayList<>();

    public SmartExpenseManagerJDBC1(){
        setTitle("Smart Expense Manager – Rainbow Charts + Themes");
        setSize(1180,700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        buildUI();
        load();
        refresh();
        setVisible(true);
    }

    // ================= UI BUILD =================
    void buildUI(){
        JPanel root=new JPanel(new BorderLayout());
        setContentPane(root);

        // HEADER
        JPanel head=new JPanel(new BorderLayout());
        head.setBackground(new Color(40,40,40));
        head.setBorder(new EmptyBorder(8,12,8,12));

        JLabel title=new JLabel("Smart Expense Manager");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI",Font.BOLD,18));

        lblTotal=smallLbl("Total: 0.00",Color.WHITE);
        lblBudget=smallLbl("Budget: 0.00",Color.LIGHT_GRAY);
        lblRemain=smallLbl("Remaining: 0.00",Color.GREEN);

        JPanel rightH=new JPanel(new GridLayout(3,1));
        rightH.setOpaque(false);
        rightH.add(lblTotal);
        rightH.add(lblBudget);
        rightH.add(lblRemain);

        head.add(title,BorderLayout.WEST);
        head.add(rightH,BorderLayout.EAST);
        root.add(head,BorderLayout.NORTH);

        JSplitPane sp=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        sp.setDividerLocation(320);
        root.add(sp);

        // LEFT PANEL (SCROLL)
        JPanel left=new JPanel(new GridLayout(0,2,6,6));
        left.setBorder(new EmptyBorder(8,8,8,8));

        tfDate=smallTF(LocalDate.now().format(Expense.FMT));
        tfTitle=smallTF("");
        tfAmount=smallTF("");
        tfNote=smallTF("");
        tfSearch=smallTF("");

        cbCat=new JComboBox<>(CATS);

        left.add(lbl("Date")); left.add(tfDate);
        left.add(lbl("Category")); left.add(cbCat);
        left.add(lbl("Title")); left.add(tfTitle);
        left.add(lbl("Amount")); left.add(tfAmount);
        left.add(lbl("Note")); left.add(tfNote);

        JButton add=btn("Add");
        JButton edit=btn("Edit");
        JButton clear=btn("Clear");

        left.add(add); left.add(edit);
        left.add(clear); left.add(new JLabel());

        JButton setB=btn("Set Budget");
        left.add(setB); left.add(new JLabel());

        left.add(lbl("Search"));
        left.add(tfSearch);

        sp.setLeftComponent(new JScrollPane(left));

        // RIGHT PANEL
        model=new DefaultTableModel(
                new String[]{"ID","Date","Category","Title","Amount","Note"},0){
            public boolean isCellEditable(int r,int c){return false;}
        };

        table=new JTable(model);
        table.setRowHeight(22);

        TableColumn idc=table.getColumnModel().getColumn(0);
        idc.setMinWidth(0);
        idc.setMaxWidth(0);

        JPanel right=new JPanel(new BorderLayout(6,6));
        right.add(new JScrollPane(table),BorderLayout.CENTER);

        JPanel top=new JPanel();
        JButton del=btn("Delete");
        JButton ref=btn("Refresh");
        top.add(del); top.add(ref);
        right.add(top,BorderLayout.NORTH);

        chart=new ChartPanel();
        right.add(chart,BorderLayout.SOUTH);

        sp.setRightComponent(right);

        // EVENTS
        add.addActionListener(e->add());
        edit.addActionListener(e->edit());
        del.addActionListener(e->delete());
        clear.addActionListener(e->clearForm());
        ref.addActionListener(e->{load();refresh();});
        setB.addActionListener(e->setBudget());

        tfSearch.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e){search();}
            public void removeUpdate(DocumentEvent e){search();}
            public void changedUpdate(DocumentEvent e){}
        });
    }

    // ================= HELPERS =================
    JTextField smallTF(String t){
        JTextField f=new JTextField(t);
        f.setPreferredSize(new Dimension(120,22));
        f.setFont(new Font("Segoe UI",Font.PLAIN,12));
        return f;
    }

    JLabel lbl(String t){
        JLabel l=new JLabel(t);
        l.setFont(new Font("Segoe UI",Font.PLAIN,12));
        return l;
    }

    JLabel smallLbl(String t,Color c){
        JLabel l=new JLabel(t);
        l.setForeground(c);
        l.setFont(new Font("Segoe UI",Font.PLAIN,12));
        return l;
    }

    JButton btn(String t){
        JButton b=new JButton(t);
        b.setFont(new Font("Segoe UI",Font.PLAIN,12));
        b.setBackground(new Color(70,70,70));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    // ================= DB =================
    void load(){
        data.clear();
        try(Connection c=con();
            Statement s=c.createStatement();
            ResultSet r=s.executeQuery("select * from expenses order by exp_date")){
            while(r.next())
                data.add(new Expense(
                        r.getInt(1),
                        r.getDate(2).toLocalDate(),
                        r.getString(3),
                        r.getString(4),
                        r.getDouble(5),
                        r.getString(6)));
        }catch(Exception e){msg(e);}
    }

    void add(){
        try(Connection c=con();
            PreparedStatement p=c.prepareStatement(
                    "insert into expenses(exp_date,category,title,amount,note) values(?,?,?,?,?)")){
            p.setDate(1,Date.valueOf(
                    LocalDate.parse(tfDate.getText(),Expense.FMT)));
            p.setString(2,cbCat.getSelectedItem().toString());
            p.setString(3,tfTitle.getText());
            p.setDouble(4,Double.parseDouble(tfAmount.getText()));
            p.setString(5,tfNote.getText());
            p.executeUpdate();
            load();refresh();clearForm();
        }catch(Exception e){msg(e);}
    }

    void edit(){
        int r=table.getSelectedRow(); if(r<0)return;
        int id=Integer.parseInt(model.getValueAt(r,0).toString());
        String a=JOptionPane.showInputDialog(this,"New Amount");
        if(a==null)return;
        try(Connection c=con();
            PreparedStatement p=c.prepareStatement(
                    "update expenses set amount=? where id=?")){
            p.setDouble(1,Double.parseDouble(a));
            p.setInt(2,id);
            p.executeUpdate();
            load();refresh();
        }catch(Exception e){msg(e);}
    }

    void delete(){
        int r=table.getSelectedRow(); if(r<0)return;
        int id=Integer.parseInt(model.getValueAt(r,0).toString());
        try(Connection c=con();
            PreparedStatement p=c.prepareStatement(
                    "delete from expenses where id=?")){
            p.setInt(1,id);
            p.executeUpdate();
            load();refresh();
        }catch(Exception e){msg(e);}
    }

    // ================= LOGIC =================
    void refresh(){
        model.setRowCount(0);
        double total=0;
        for(Expense e:data){
            model.addRow(new Object[]{
                    e.id,e.d.format(Expense.FMT),
                    e.c,e.t,e.a,e.n});
            total+=e.a;
        }
        lblTotal.setText("Total: "+String.format("%.2f",total));
        lblBudget.setText("Budget: "+String.format("%.2f",budget));
        double rem=budget-total;
        lblRemain.setText("Remaining: "+String.format("%.2f",rem));
        lblRemain.setForeground(rem<0?Color.RED:Color.GREEN);
        chart.repaint();
    }

    void search(){
        String q=tfSearch.getText().toLowerCase();
        model.setRowCount(0);
        for(Expense e:data)
            if(e.t.toLowerCase().contains(q)||e.c.toLowerCase().contains(q))
                model.addRow(new Object[]{
                        e.id,e.d.format(Expense.FMT),
                        e.c,e.t,e.a,e.n});
    }

    void clearForm(){
        tfDate.setText(LocalDate.now().format(Expense.FMT));
        tfTitle.setText("");
        tfAmount.setText("");
        tfNote.setText("");
        cbCat.setSelectedIndex(0);
        table.clearSelection();
    }

    void setBudget(){
        String input=JOptionPane.showInputDialog(
                this,"Enter your budget","Set Budget",
                JOptionPane.PLAIN_MESSAGE);
        if(input==null||input.trim().isEmpty())return;
        try{
            budget=Double.parseDouble(input);
            refresh();
            JOptionPane.showMessageDialog(
                    this,"Your budget is set to "+
                            String.format("%.2f",budget));
        }catch(Exception e){msg(e);}
    }

    // ================= ERROR HANDLER =================
    void msg(Exception e){
        JOptionPane.showMessageDialog(
                this,e.getMessage(),
                "Error",JOptionPane.ERROR_MESSAGE);
    }

    // ================= CHART =================
    class ChartPanel extends JPanel{
        ChartPanel(){
            setPreferredSize(new Dimension(100,180));
            setBorder(new TitledBorder("Category Chart"));
        }
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g;

            Map<String,Double> m=new LinkedHashMap<>();
            for(String c:CATS)m.put(c,0.0);
            for(Expense e:data)
                m.put(e.c,m.get(e.c)+e.a);

            int w=getWidth()-40;
            int h=getHeight()-40;
            int x=20;
            int bw=w/CATS.length;

            double max=m.values().stream()
                    .mapToDouble(v->v).max().orElse(1);

            int i=0;
            for(String c:CATS){
                int bh=(int)((m.get(c)/max)*h);
                Color c1=Color.getHSBColor(i/7f,0.9f,0.9f);
                Color c2=Color.getHSBColor((i+1)/7f,0.7f,0.6f);

                g2.setPaint(new GradientPaint(x,0,c1,x,getHeight(),c2));
                g2.fillRoundRect(
                        x,getHeight()-20-bh,
                        bw-10,bh,12,12);

                g2.setColor(Color.BLACK);
                g2.drawString(c,x,getHeight()-5);
                x+=bw; i++;
            }
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(SmartExpenseManagerJDBC1::new);
    }
}
