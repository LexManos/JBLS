package Hashing;
public class lockdown_heap{
  private char[] memory = null;
  private int currentlength = 0;
  private int maximumlength = 0;
  private boolean autosort = false;

  public lockdown_heap(boolean s){
    autosort = s;
    memory = new char[0x1000];
    currentlength = 0;
    maximumlength = 0x100;
  }
  public lockdown_heap(){
    autosort = false;
    memory = new char[0x1000];
    currentlength = 0;
    maximumlength = 0x100;
  }
  public void destroy(){
    memory = null;
    System.gc();
  }
  public int length(){ return currentlength; }
  public int[] toIntArray(){
//    System.out.println(currentlength + " " + (currentlength*4));
    int[] buff = new int[currentlength*4];
    for(int x = 0; x < (currentlength*4); x++){
      buff[x]  =  (int)memory[x*4]          & 0x000000FF;
      buff[x] |= ((int)memory[x*4+1] << 8)  & 0x0000FF00;
      buff[x] |= ((int)memory[x*4+2] << 16) & 0x00FF0000;
      buff[x] |= ((int)memory[x*4+3] << 24) & 0xFF000000;
    }
    return buff;
  }
  public void resize(){
    char[] tmp = new char[currentlength*0x10];
//    System.out.println("Resized: " + Integer.toHexString(memory.length) + " -> " + Integer.toHexString(currentlength*0x10));
    System.arraycopy(memory, 0, tmp, 0, currentlength*0x10);
    this.memory = tmp;
  }
  
  public void add(char[] data){
    if(currentlength + 0x10 >= maximumlength){
      maximumlength *= 2;
//      System.out.println("Heap: " + Integer.toHexString(currentlength) + " " + Integer.toHexString(maximumlength));
      char[] newbuff = new char[maximumlength*0x10];
      System.arraycopy(memory, 0, newbuff, 0, memory.length);
      memory = newbuff;
      System.gc();
    }
    if(autosort){
      int insert = findLocation(getInt(data, 0));
      for(int x = currentlength; x >= insert; x--)
        swap(x+1, x);
      System.arraycopy(data, 0, memory, insert*0x10, 0x10);
//      System.out.print("Add " + insert + " " + getInt(data, 0) + " "); utile.print_hash(data);
    }else{
      System.arraycopy(data, 0, memory, currentlength*0x10, 0x10);
    }
    currentlength++;
  }
  
  private int findLocation(int num){
    for(int x = -1; x < currentlength; x++){
//      System.out.println(num + " " + getInt(x*0x10) + " " + getInt((x+1)*0x10));
      if(getInt(x*0x10) < num && getInt((x+1)*0x10) >= num)
        return x+1;
    }
    return currentlength;
  }
  
  public void sort(){ quicksort(0, currentlength-1); }
  private void quicksort(int l, int r){
    if(l>=r) return;
    int m = partition(l, r);
//    System.out.println(Integer.toHexString(m));
    quicksort(l, m-1);
    quicksort(m+1, r);
  }
  private int partition(int l, int r){
    int i = l+1;
    int j = r;
    int p = getInt(l*0x10);
    while(i<=j){
//      System.out.println(Integer.toHexString(p) + " " + Integer.toHexString(getInt(i*0x10)) + " " + Integer.toHexString(getInt(j*0x10)));
      if(getInt(i*0x10)<=p) i++;
      else if(getInt(j*0x10)>p) j--;
      else swap(i, j);
    }
    swap(l, j);
    return j;
  }
  private void swap(int i, int j){
    char[] h = new char[0x10];
    //System.out.println( "Swap: " + Integer.toHexString(getInt(i*0x10)) + " -> " + Integer.toHexString(getInt(j*0x10)));
    System.arraycopy(memory, i*0x10, h, 0, 0x10);
    System.arraycopy(memory, j*0x10, memory, i*0x10, 0x10);
    System.arraycopy(h, 0, memory, j*0x10, 0x10);
  }
  
  public void print(){
    for(int i = 0; i < currentlength; i++){
      for(int j = 0; j < 0x10; j++)
        System.out.print( (memory[i*0x10+j] < 0x10 ? "0" : "") + Integer.toHexString(memory[i*0x10+j]) + " ");
      System.out.print("\t");
      for(int j = 0; j < 0x10; j++)
        System.out.print((memory[i*0x10+j] < 0x20 || memory[i*0x10+j] > 0x7F ? "." : memory[i*0x10+j]));
      System.out.println("");
    }
	  System.out.println("Length: " + (currentlength * 0x10) + " bytes");
  }
  
  private int getInt(int o){ return getInt(memory, o); }
  private int getInt(char[] m, int o){
    if(o < 0) return 0;
    int 
    sig  = ((int)m[o+0] << 0)  & 0x000000FF;
    sig |= ((int)m[o+1] << 8)  & 0x0000FF00;
    sig |= ((int)m[o+2] << 16) & 0x00FF0000;
    sig |= ((int)m[o+3] << 24) & 0xFF000000;
    return sig;
  }
}