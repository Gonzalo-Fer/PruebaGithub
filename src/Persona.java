public class Persona {

    private String nombre;
    private String apellido;
    private int edad;
    private String email;
    private String dni;

    public Persona(String n,String a, int e, String em, String dni){
        this.nombre=n;
        this.apellido=a;
        this.edad=e;
        this.dni=dni;
        this.email=em;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

}
