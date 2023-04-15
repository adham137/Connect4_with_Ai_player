/*------------------------------------------------------------------------------------
Functions implemented:

    1. start:
        input: Stage object
        funciton:    construct the gui of the game

    2. initializeButtons:
        input:    GridPane object
        function: create 7x6 buttons , add them to buttons arraylist

    3. giveButtonsFunctionality:
        input:    Label object (responsible for displaying info about the game)
        function: upon buttons press
                  1. a button can only be pressed if it's in the bottom row or the bottom bellow it is pressed
                  2. change colour of button to red/yellow
                  3. input into currentBoard matrix
                  4. disable pressed button
                  5. check for a winner
                  6. change turn
                  7. update playableButtons array
                  8. if it's red turn , invoke AI

    4. updatePlayableButtons:
        input: 1. boolean forward: if true, refers to removing a button and adding the button above it ,
                                   if false, then add a button and remove button above it.
               2. rowIndex:        row index of button in currentBoard matrix
               3. colIndex:        column index of button in currentBoard matrix
               4. buttonIndex:     index of button in buttons arraylist , only needed if forward is false.

        function: update playableButtons arraylist, which contains the list of buttons which can currently
                  be pressed and is updated as we press buttons.

    5. checkForWinner:
        input:    color of the current player
        function: check horizontally, vertically and diagonally for a 4 buttons match
        outputs:  red match: 1
                  yellow match: -1
                  draw: 2

     6. AiFunction:
        input:    depth of the search tree
        function: preform first maximising step of the minimax algorithm , obtain
                  the best move as a button index and press it.

    7. MiniMax:
        input: 1. isMaximising: is the current step a maximising step
               2. depth: current depth
               3. alpha: best move yet for maximising player
               4. beta: best move yet for minimising player
        function: preform the minimax algorithm to obtain a score for moves and determine
                  the best move based on it.
        output:   score as an integer

    8. main:
        function: launch the application

    9. scorePosition:
        input:    current turn
        function: score current board if there is no winner yet, scoring is based on
                  if the player pieces occupy the middle column, splitting the board
                  into windows and evaluating each window separately.
        output:   score

    10. evaluateWindow:
        input:    arraylist of size for named window, current turn
        function: score the window based on the number of player pieces in it
                  (the more the better).
        output:   score


------------------------------------------------------------------------------------*/
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;


public class Connect4Application extends Application{

    private String turn = "Yellow";

    private ArrayList<Button> buttons= new ArrayList<>();

    //contains row and column indices of playable buttons
    private ArrayList<String> playableButtons = new ArrayList<>();

    private short [][] currentBoard = new short [6][7];

    short emptySpacesOnBoard = 42 ;


    public void start(Stage window){
        //create main pane and top text field
        BorderPane mainPain = new BorderPane();
        Label topTextField = new Label("Turn: "+turn);
        topTextField.setFont(Font.font("Monospaced", 50));
        topTextField.setPadding(new Insets(20, 20, 20, 75));

        //create the grid pane that houses the buttons
        GridPane secondaryPane = new GridPane();
        secondaryPane.setAlignment(Pos.CENTER);
        // set it's background colour to blue
        BackgroundFill background_fill = new BackgroundFill(Paint.valueOf("BLUE"),
                CornerRadii.EMPTY, Insets.EMPTY);
        Background background = new Background(background_fill);
        secondaryPane.setBackground(background);
        //space the buttons
        secondaryPane.setHgap(10);
        secondaryPane.setVgap(10);

        //create the buttons
        initializeButtons(secondaryPane);
        //give buttons functionality
        giveButtonsFunctionality(topTextField);

        mainPain.setTop(topTextField);
        mainPain.setCenter(secondaryPane);

        mainPain.setPrefSize(1000,1000);
        mainPain.setPadding(new Insets(20, 20, 20, 20));

        Scene scene = new Scene(mainPain);
        window.setTitle("Connect-4 Game");
        window.setScene(scene);
        window.show();

    }

    public void initializeButtons(GridPane secondaryPane){
        for(int r=0; r<6 ; r++){
            for(int c=0 ; c<7 ; c++){
                Button newButton = new Button(r+" "+c); //index of buttons is saved inside it
                newButton.setPrefSize(120,120);
                newButton.setShape(new Circle(120));
                secondaryPane.add(newButton,c,r);
                buttons.add(newButton);
                if (r==5) playableButtons.add(newButton.getText());

            }
        }
    }
    //--------------------------------------------------------------------------

    public void giveButtonsFunctionality(Label topTextField){

        BackgroundFill background_fill_yellow = new BackgroundFill(Paint.valueOf("YELLOW"),
                CornerRadii.EMPTY, Insets.EMPTY);

        BackgroundFill background_fill_red = new BackgroundFill(Paint.valueOf("RED"),
                CornerRadii.EMPTY, Insets.EMPTY);

        buttons.forEach(button -> button.setOnAction(event -> {

            short rowIndex = Short.valueOf( button.getText().split(" ")[0] );
            short colIndex = Short.valueOf( button.getText().split(" ")[1] );

            // 1. a button can not be pressed if it's not in the bottom row (row 5)
            //    and the bottom below it is not pressed.
                if( ( rowIndex != 5 ) && ( currentBoard [rowIndex+1][colIndex] == 0 ) ) return;


            // 2. change colour of button to red/yellow upon press
            if(turn.equals("Red")){
                button.setBackground(new Background(background_fill_red));
            }
            else{
                button.setBackground(new Background(background_fill_yellow));
            }

            // 3. input into currentBoard matrix
            currentBoard [ rowIndex ][ colIndex ] = (short) ((turn == "Red")? 1 : -1);

            // 4. disable the pressed button
            button.setMouseTransparent(true);

            // 5. check for a winner
            switch(checkForWinner(turn)) {
                case 1:
                    topTextField.setText("Ai Has Won!");
                    buttons.forEach(b -> b.setDisable(true));
                    return;

                case -1:
                    topTextField.setText("Human Has Won!");
                    buttons.forEach(b -> b.setDisable(true));
                    return;

                case 0:
                    topTextField.setText("Draw!");
                    buttons.forEach(b -> b.setDisable(true));
                    return;

            }

            // 6. change turn
            turn = ((turn == "Red")? "Yellow" : "Red");
            topTextField.setText("Turn: "+turn);

            // 7. update playableButtons array
            updatePlayableButtons(true, rowIndex, colIndex, 0);

            // 8. if it's red turn , invoke AI
             if ( turn.equals("Red") ) AiFunction(5);

        }));
    }
    //------------------------------------------------------------------------------------

    public void updatePlayableButtons (boolean forward, short rowIndex, short colIndex, int buttonIndex){

        if (forward){

            playableButtons.remove(rowIndex + " " + colIndex);
            if( rowIndex > 0 ) playableButtons.add( (rowIndex-1)+" "+colIndex );
            emptySpacesOnBoard--;

        }else{

            emptySpacesOnBoard++;
            playableButtons.add(buttonIndex,rowIndex+" "+colIndex);
            if( rowIndex > 0 ) playableButtons.remove( (rowIndex-1)+" "+colIndex );

        }



        //-----------------------------------------------------
        System.out.print("Playable indices: ");
        for(String playableButton : playableButtons){
            System.out.print(playableButton+" - ");
        }
        System.out.println(" ");
        System.out.println("Empty Spaces left: "+emptySpacesOnBoard);
        for(int r=0; r<6 ; r++){
            for(int c=0 ; c<7 ; c++){
                System.out.print(currentBoard[r][c]+" ");
            }
            System.out.println("");
        }
        System.out.println("--------------------------------------------");

    }

    //------------------------------------------------------------------------------------
    public short checkForWinner(String colorTurn){
        short player = (short) ((colorTurn.equals("Red"))? 1 : -1);


        // horizontalCheck
        for (int j = 0; j<7-3 ; j++ ){
            for (int i = 0; i<6; i++){
                if (currentBoard [i][j] == player && currentBoard [i][j+1] == player && currentBoard [i][j+2] == player && currentBoard [i][j+3] == player){
                    return player;
                }
            }
        }
        // verticalCheck
        for (int i = 0; i<6-3 ; i++ ){
            for (int j = 0; j<7; j++){
                if (currentBoard [i][j] == player && currentBoard [i+1][j] == player && currentBoard [i+2][j] == player && currentBoard [i+3][j] == player){
                    return player;
                }
            }
        }
        // ascendingDiagonalCheck
        for (int i=3; i<6; i++){
            for (int j=0; j<7-3; j++){
                if (currentBoard [i][j] == player && currentBoard [i-1][j+1] == player && currentBoard [i-2][j+2] == player && currentBoard [i-3][j+3] == player)
                    return player;
            }
        }
        // descendingDiagonalCheck
        for (int i=3; i<6; i++){
            for (int j=3; j<7; j++){
                if (currentBoard [i][j] == player && currentBoard [i-1][j-1] == player && currentBoard [i-2][j-2] == player && currentBoard [i-3][j-3] == player)
                    return player;
            }
        }
        return (short) ((emptySpacesOnBoard == 0)? 0 : 2);
    }

    //------------------------------------------------------------------------------------

    public void AiFunction(int searchingDepth){

        int bestValue = -999;
        short bestButtonIndex = 0;

        for (int i=0 ; i<playableButtons.size() ; i++){

            short rowI = Short.valueOf(playableButtons.get(i).split(" ")[0]);
            short colI = Short.valueOf(playableButtons.get(i).split(" ")[1]);

            currentBoard [rowI][colI] = 1;
            updatePlayableButtons(true, rowI, colI, 0);

            int value = MiniMax(false, searchingDepth , -999 , 999);

            currentBoard [rowI][colI] = 0;
            updatePlayableButtons(false, rowI, colI, i);

            if(value > bestValue){
                bestValue = value;
                bestButtonIndex = (short) ((7*rowI)+colI);
            }

        }

        buttons.get( bestButtonIndex ).fire();
    }

    //------------------------------------------------------------------------------------

    public int MiniMax(boolean isMaximizing , int depth , int alpha , int beta){

        short x;
        //if there's a winner return
        if (isMaximizing) x = checkForWinner("Red");
        else x = checkForWinner("Yellow");

        if( (x != 2) || (depth == 0) ){

            if ( x== 0 ) return 0;
            else if ( x == 1 ) return 999;
            else if ( x == -1 ) return -999;
            else return scorePosition("Red");

        }

        if(isMaximizing){

            int bestValue = -999;
            for (int i=0 ; i<playableButtons.size() ; i++){

                short rowI = Short.valueOf(playableButtons.get(i).split(" ")[0]);
                short colI = Short.valueOf(playableButtons.get(i).split(" ")[1]);

                currentBoard [rowI][colI] = 1;
                updatePlayableButtons(true, rowI, colI, 0);

                int value = MiniMax(false, depth-1 , alpha ,beta);

                currentBoard [rowI][colI] = 0;
                updatePlayableButtons(false, rowI, colI, i);

                bestValue = Math.max(bestValue,value);
                alpha = Math.max(alpha,bestValue);
                if ( beta <= alpha ) break;

            }
            return bestValue;

        }
        else{
            int bestValue = 999;
            for (int i=0 ; i<playableButtons.size() ; i++){

                short rowI = Short.valueOf(playableButtons.get(i).split(" ")[0]);
                short colI = Short.valueOf(playableButtons.get(i).split(" ")[1]);

                currentBoard [rowI][colI] = -1;
                updatePlayableButtons(true, rowI, colI, 0);


                int value = MiniMax(true, depth-1 , alpha , beta);

                currentBoard [rowI][colI] = 0;
                updatePlayableButtons(false, rowI, colI, i);

                bestValue = Math.min(bestValue,value);
                beta = Math.min(beta,bestValue);
                if ( beta <= alpha ) break;

            }
            return bestValue;
        }
    }

    //------------------------------------------------------------------------------------
    public static void main(String[] args) {

        launch(Connect4Application.class);

    }
    //------------------------------------------------------------------------------------
    int evaluate_window(ArrayList<Short> window, String turn){

        int score = 0;
        short piece;
        short opp_piece;

        if(turn.equals("Red")){
            piece = 1;
            opp_piece = -1;
        }
        else{
            piece = -1;
            opp_piece = 1;
        }
        short numOfPiecesInWindow = (short) window.stream().filter(s->s == piece).count();
        short numOfEmptyPiecesInWindow = (short) window.stream().filter(s->s == 0).count();

        if( (numOfPiecesInWindow == 3) && (numOfEmptyPiecesInWindow == 1)) score += 5;
        else if( (numOfPiecesInWindow == 2) && (numOfEmptyPiecesInWindow == 2)) score += 2;


        if( (window.stream().filter(s->s == opp_piece).count() == 3) && (numOfEmptyPiecesInWindow == 1)) score -= 4;

        return score;

    }

    //------------------------------------------------------------------------------------

    int scorePosition ( String turn ){

        int score = 0;
        short piece  = (short) ((turn.equals("Red"))? 1 : -1);

        //score centre column , try to maximize pieces there
        short centerCount=0;
        for(int i=0 ; i<6 ; i++){
            if( currentBoard [i][3] == piece) centerCount++;
        }
        score += centerCount * 2;

        //score horizontal
        for (int i=0 ; i<6 ;i++){
            ArrayList<Short> rowArray = new ArrayList<>();
            Collections.addAll(rowArray , currentBoard[i][0],currentBoard[i][1],currentBoard[i][2],currentBoard[i][3],currentBoard[i][4],currentBoard[i][5],currentBoard[i][6]);

            short windowLength = 4;
            for(int c=0 ; c<(7-3) ; c++){
                ArrayList <Short> window = new ArrayList<Short>( rowArray.subList(c,c+4) );
                score += evaluate_window(window,turn);
            }
        }

        //score vertical
        for (int i=0 ; i<7 ;i++){
            ArrayList<Short> colArray = new ArrayList<>();
            Collections.addAll(colArray , currentBoard[0][i],currentBoard[1][i],currentBoard[2][i],currentBoard[3][i],currentBoard[4][i],currentBoard[5][i]);

            short windowLength = 4;
            for(int c=0 ; c<(6-3) ; c++){
                ArrayList <Short> window = new ArrayList<Short>( colArray.subList(c,c+4) );
                score += evaluate_window(window,turn);
            }
        }

        //score positive diagonal
        for (int r=0 ; r < 6-3  ;r++){

            for (int c=0 ; c < 7-3 ; c++) {

                ArrayList<Short> window = null;
                for (int i = 0; i < 4; i++) {

                    window = new ArrayList<>();
                    window.add(currentBoard[r + i][c + i]);
                }
                score += evaluate_window(window, turn);
            }
        }
        for (int r=0 ; r < 6-3  ;r++){

            for (int c=0 ; c < 7-3 ; c++) {

                ArrayList<Short> window = null;
                for (int i = 0; i < 4; i++) {

                    window = new ArrayList<>();
                    window.add(currentBoard[r+3-i][c + i]);
                }
                score += evaluate_window(window, turn);
            }
        }

        return score;

    }



}
