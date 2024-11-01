#include "main_Board.h"

#include <vector>
#include <iostream>
#include <string>
#include <unordered_map>

using namespace std;

vector<vector<char>> placing(vector<vector<char>>& board);

const int SIZE = 8;

vector board(8, std::vector(8, '.'));
int white = 12;
int black = 12;

const char white_val = 'W';
const char black_val = 'B';
const char empty_val = '.';

bool whiteTurn = true;



vector<vector<char>> placing(vector<vector<char>>& board) {

  for (auto i = 0; i < 3; ++i) {
    for (auto j = 0; j < SIZE; ++j) {
      if((i + j) % 2 == 1) {
        board[i][j] = black_val;
      }else {
        board[i][j] = empty_val;
      }
    }
  }

  for (auto i = 3; i < 5; ++i) {
    for (auto j = 0; j < SIZE; ++j) {
      board[i][j] = empty_val;
    }
  }

  for (auto i = 5; i < 8; ++i) {
    for (auto j = 0; j < SIZE; ++j) {
      if((i + j) % 2 == 1) {
        board[i][j] = white_val;
      }
      else {
        board[i][j] = empty_val;
      }
    }
  }

  return board;
}

bool isChosenCheckTurn(int row, int col) {
  if(whiteTurn && board[row][col] == white_val) {
    return true;
  }
  return !whiteTurn && board[row][col] == black_val;
}

bool isValidMove(int row, int col) {
  return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
}

unordered_map<string,vector<vector<int>>> MovePrediction(int row, int col) {
  unordered_map<string, vector<vector<int>>> moves;
  vector<vector<int>> regularMoves;
  vector<vector<int>> captures;
  char piece = board[row][col];

  vector<pair<int, int>> forwardDirections;
  vector<pair<int, int>> captureDirections;

  if (piece == black_val) {
    forwardDirections = {{1, -1}, {1, 1}};
    captureDirections = {{1, -1}, {1, 1}, {-1, -1}, {-1, 1}};
  } else if (piece == white_val) {
    forwardDirections = {{-1, -1}, {-1, 1}};
    captureDirections = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
  }

  for (const auto& dir : forwardDirections) {
    int newRow = row + dir.first;
    int newCol = col + dir.second;

    if (isValidMove(newRow, newCol) && board[newRow][newCol] == empty_val) {
      regularMoves.push_back({newRow, newCol});
    }
  }

  for (const auto& dir : captureDirections) {
    int adjacentRow = row + dir.first;
    int adjacentCol = col + dir.second;
    int jumpRow = row + 2 * dir.first;
    int jumpCol = col + 2 * dir.second;

    if (isValidMove(jumpRow, jumpCol) && board[jumpRow][jumpCol] == empty_val) {
      char opponentPiece = board[adjacentRow][adjacentCol];
      if ((piece == black_val && opponentPiece == white_val) ||
          (piece == white_val && opponentPiece == black_val)) {
        captures.push_back({jumpRow, jumpCol});
          }
    }
  }

  moves["regular_moves"] = regularMoves;
  moves["captures"] = captures;

  return moves;
}

vector<int> captureRule(int rowM, int colM) {
  vector<int> result;

  for (int row = 0; row < SIZE; row++) {
    for (int col = 0; col < SIZE; col++) {
      bool isCurrentPlayerPiece = (whiteTurn && board[row][col] == white_val) ||
                                  (!whiteTurn && board[row][col] == black_val);

      if (isCurrentPlayerPiece && !(row == rowM && col == colM)) {
        auto temp = MovePrediction(row, col);

        if (!temp["captures"].empty()) {
          result.push_back(row);
          result.push_back(col);
          return result;
        }
      }
    }
  }
  return result;
}

JNIEXPORT jboolean JNICALL Java_main_Board_isValidPieceToSelect
  (JNIEnv *evt, jobject obj, jint jrow, jint jcol) {
  if(whiteTurn && (board[jrow][jcol] == 'W' || board[jrow][jcol] == 'Q')) {
    return true;
  }
  if(!whiteTurn && (board[jrow][jcol] == 'B' || board[jrow][jcol] == 'K')) {
    return true;
  }
  return false;
}

JNIEXPORT jobjectArray JNICALL Java_main_Board_placingPieces
  (JNIEnv *env, jclass obj){

  auto temp = placing(board);

  auto const jboard = env->NewObjectArray(SIZE, env->FindClass("[C"), nullptr);

  for (auto i = 0; i < SIZE; i++) {
    auto const row = env->NewCharArray(SIZE);
    vector<jchar> filled(temp[i].begin(), temp[i].end());

    env->SetCharArrayRegion(row, 0, SIZE, filled.data());
    env->SetObjectArrayElement(jboard, i, row);
    env->DeleteLocalRef(row);
  }

  return jboard;
}

JNIEXPORT void JNICALL Java_main_Board_move
  (JNIEnv *env, jobject obj, jint from_row, jint from_col, jint to_row, jint to_col, jboolean change) {
  auto piece= board[from_row][from_col];

  board[to_row][to_col] = piece;
  board[from_row][from_col] = empty_val;

  if(change){whiteTurn = !whiteTurn;}
}

JNIEXPORT jobject JNICALL Java_main_Board_predictedMoves
  (JNIEnv *env, jobject obj, jint jrow, jint jcol) {


  unordered_map<string, vector<vector<int>>> moves = MovePrediction(jrow, jcol);

  jclass hashMapClass = env->FindClass("java/util/HashMap");
  jmethodID hashMapConstructor = env->GetMethodID(hashMapClass, "<init>", "()V");
  jmethodID hashMapPut = env->GetMethodID(hashMapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

  jobject resultMap = env->NewObject(hashMapClass, hashMapConstructor);

  for (const auto& entry : moves) {
    const string& key = entry.first;
    const vector<vector<int>>& moveList = entry.second;

    jobjectArray javaMoveArray = env->NewObjectArray(moveList.size(), env->GetObjectClass(env->NewIntArray(2)), nullptr);

    for (size_t i = 0; i < moveList.size(); ++i) {
      jintArray movePair = env->NewIntArray(2);
      env->SetIntArrayRegion(movePair, 0, 2, moveList[i].data());
      env->SetObjectArrayElement(javaMoveArray, i, movePair);
      env->DeleteLocalRef(movePair);
    }


    jstring javaKey = env->NewStringUTF(key.c_str());


    env->CallObjectMethod(resultMap, hashMapPut, javaKey, javaMoveArray);


    env->DeleteLocalRef(javaKey);
    env->DeleteLocalRef(javaMoveArray);
  }

  return resultMap;
}

JNIEXPORT void JNICALL Java_main_Board_removeCaptured
  (JNIEnv *, jobject, jint row, jint col) {
  board[row][col] = empty_val;
  if (whiteTurn) white--;
  else black--;
}

JNIEXPORT jintArray JNICALL Java_main_Board_captureRule
  (JNIEnv *env, jobject obj, jint rowDest, jint colDest) {
  auto vec = captureRule(rowDest, colDest);

  auto jres = env->NewIntArray(vec.size());

  env->SetIntArrayRegion(jres, 0, vec.size(), vec.data());

  return jres;
}

JNIEXPORT jint JNICALL Java_main_Board_winner
  (JNIEnv *env, jobject obj) {
    if(black == 0)return 2;
    if(white == 0)return 1;
    return 0;
}

JNIEXPORT jobjectArray JNICALL Java_main_Board_displaying
  (JNIEnv *env, jobject obj) {
  auto temp = board;

  auto const jboard = env->NewObjectArray(SIZE, env->FindClass("[C"), nullptr);

  for (auto i = 0; i < SIZE; i++) {
    auto const row = env->NewCharArray(SIZE);
    vector<jchar> filled(temp[i].begin(), temp[i].end());

    env->SetCharArrayRegion(row, 0, SIZE, filled.data());
    env->SetObjectArrayElement(jboard, i, row);
    env->DeleteLocalRef(row);
  }

  return jboard;
}

JNIEXPORT void JNICALL Java_main_Board_restart
  (JNIEnv *env, jobject obj) {
  vector temp(8, vector(8, '.'));
  board = temp;

  placing(board);
  whiteTurn = true;
}

JNIEXPORT jboolean JNICALL Java_main_Board_getTurn
  (JNIEnv *env, jobject) {
  return whiteTurn;
}
