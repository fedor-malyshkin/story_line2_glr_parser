package ru.nlp_project.story_line2.glr_parser.eval;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ru.nlp_project.story_line2.glr_parser.SymbolExt;
import ru.nlp_project.story_line2.glr_parser.Token;

/**
 * ИНтерфейс объекта-анализатора для проверки соотвествия помет, помет-ограничений 
 * токена подаваемого на входе набору правил перехода FSM грамматики. 
 * 
 * @author fedor
 *
 */
public interface IGLRTokenMatcher {

  /**
   * Сформировать список действий для анализатора в зависимости от постуапающего токена 
   * и имеющегося у него грамматических свойств.
   * 
   * Вкратце алгоритм такой: 
   * <ol>
   * <li>анализируем токен</li>
   * <li>проверяем наличие в @actionTableEntry соответствующего символа в качестве ключа</li>
   * <li>при наличии - добавляем в результат значение по ключу</li>
   * <li>Tip: по умолчанию добавляются все значения с ключем {@link IGLRTokenMatcher#SMB_ANY_WORD}</li>
   * </ol>
   * 
   * 
   * Ннобходимо учитывать, что после определенной воерсии парсера в качестве 
   * ключей используются не простые символы ({@link Symbol}), а расширенные 
   * символы ({@link SymbolExt}). Для того, что бы избежать сквозного перебора 
   * по ключам  передается параметр @param baseNTToNTExtDataMap, содержащий 
   * записи типа "{verb=[verb<rt, gnc-agr[1]>], Ar=[Ar<h-reg1>]}, {adj=[adj]}, ...".
   * Т.е. если записи тим нет, то и в таблице переходов ничего нет, а если есть,
   * то надо использовать его значения для дополнительной проверки.
   * 
   * @param actionTableEntry строка таблицы с действиями (для данного 
   * номера состояния)
   * @param baseNTToNTExtDataMap маппинг простых символов в символы с 
   * расширенной информацией (для данного номера состояния)
   * @param token текущий входящий токен
   * @return результатирующие действия или null в случае отсуствия
   */
  Collection<ActionRecord> getActionTableRecords(
      Map<Symbol, List<ActionRecord>> actionTableEntry,
      Map<Symbol, List<Symbol>> baseNTToNTExtDataMap, Token token);

}
