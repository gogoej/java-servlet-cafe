package com.cafe.members;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cafe.auth.SessionAuthInfo;
import com.cafe.menu.MenuDAO;
import com.cafe.menu.MenuDTO;
import com.util.EspressoServlet;
import com.util.Pager;

@WebServlet("/members/*")
public class MembersServlet extends EspressoServlet {

	// PATH
	private static final String API_NAME = "/members";
	private static final String CAFE = "cafe";
	private static final String VIEW = "/WEB-INF/views";
	private static final String VIEWS = VIEW + "/" + CAFE;

	// PATH(dynamic)
	private static String contextPath;
	private static String apiPath;

	// API
	private static final String API_INDEX = "/index.do";
	private static final String API_LIST = "/list.do";
	private static final String API_DETAIL = "/detail.do";
	private static final String API_MODIFY_CARD_NAME = "/modifyCardName.do";
	private static final String API_REGISTER = "/register.do";
	private static final String API_CHARGE = "/charge.do";
	private static final String API_CHARGE_OK = "/charge_ok.do";
	private static final String API_ORDER = "/order.do";
	private static final String API_BUY = "/buy.do";
	private static final String API_BUY_OK = "/buy_ok.do";
	private static final String API_ORDERED_LIST = "/orderedList.do";
	private static final String API_CLOSE_CARD = "/close.do";
	private static final String API_CLOSE_CARD_OK = "/close_ok.do";

	// JSP
	private static final String JSP_LIST = "/members_list.jsp";
	private static final String JSP_DETAIL = "/members_detail.jsp";
	private static final String JSP_REGISTER_STEP1 = "/members_register_step1.jsp";
	private static final String JSP_CHARGE = "/members_charge.jsp";
	private static final String JSP_ORDER = "/members_order.jsp";

	// PARAM
	private static final String PARAM_MODE = "mode";
	private static final String PARAM_MODEL_NUM = "modelNum";
	private static final String PARAM_CARD_NUM = "cardNum";
	private static final String PARAM_TARGET_CARD_NUM = "targetCardNum";
	private static final String PARAM_MODE_REGISTER = "register";
	private static final String PARAM_MODE_CHARGE = "charge";
	private static final String PARAM_MODE_CLOSE = "close";
	private static final String PARAM_REGISTER_STEP = "register_step";
	private static final String PARAM_CARD_NAME = "cardName";
	private static final String PARAM_PRICE = "price";
	private static final String PARAM_MENU_NUM = "menuNum";
	private static final String PARAM_TAB = "tab";
//	private static final String PARAM_TAB_USAGE = "usage";
//	private static final String PARAM_TAB_CHARGE = "charge";
	private static final int PARAM_REGISTER_STEP_1 = 1;
	private static final int PARAM_REGISTER_STEP_2 = 2;
	private static final int PARAM_REGISTER_STEP_3 = 3;

	// ATTRIBUTE
	private static final String ATTRIBUTE_API = "api";
	private static final String ATTRIBUTE_LIST = "list";
	private static final String ATTRIBUTE_ORDER_HISTORY = "orderHistory";
	private static final String ATTRIBUTE_CARD_CHARGE_LIST = "cardChargeList";
	private static final String ATTRIBUTE_CARDS = "cards";
	private static final String ATTRIBUTE_ERROR_MSG = "errorMessage";
	private static final String ATTRIBUTE_CARD_DTO = "cardDTO";
	private static final String ATTRIBUTE_CARD_MODEL_DTO = "modelDTO";
	private static final String ATTRIBUTE_MAX_ITEM_AMOUNT = "maxItemAmount";

	//�⺻ �Ӽ�
	private static final int MAX_BALANCE = 550000;
	private static final int MAX_ITEM_AMOUNT = 15; // �ִ� ���� ���� ����

	// ��ٱ��� ����
	private static final String SESSION_CART = "cart";

	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		contextPath = req.getContextPath();
		apiPath = contextPath + API_NAME;
		String uri = req.getRequestURI();
		Map<String, Object> attributes = new HashMap<>();
		attributes.put(ATTRIBUTE_API, uri.substring(uri.lastIndexOf("/")));
		SessionAuthInfo info = getSessionAuthInfo(req);
		if (info == null) {
			goToLogin(resp);
			return;
		}

		if (uri.indexOf(API_INDEX) != -1 || uri.indexOf(API_LIST) != -1) {
			list(req, resp, attributes);
		} else if (uri.indexOf(API_DETAIL) != -1) {
			detail(req, resp, attributes);
		} else if (uri.indexOf(API_REGISTER) != -1) {
			register(req, resp, attributes);
		} else if (uri.indexOf(API_CHARGE) != -1) {
			chargeForm(req, resp, attributes);
		} else if (uri.indexOf(API_CHARGE_OK) != -1) {
			chargeSubmit(req, resp, attributes);
		} else if (uri.indexOf(API_ORDER) != -1) {
			order(req, resp, attributes);
		} else if (uri.indexOf(API_BUY) != -1) {
			buyForm(req, resp, attributes);
		} else if (uri.indexOf(API_BUY_OK) != -1) {
			buySubmit(req, resp, attributes);
		} else if (uri.indexOf(API_ORDERED_LIST) != -1) {
			orderedList(req, resp, attributes);
		} else if (uri.indexOf(API_CLOSE_CARD) != -1) {
			closeForm(req, resp, attributes);
		} else if (uri.indexOf(API_CLOSE_CARD_OK) != -1) {
			closeSubmit(req, resp, attributes);
		} else if (uri.indexOf(API_MODIFY_CARD_NAME)!=-1) {
			updateCardName(req, resp, attributes);
		}
	}

	protected void list(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> attributes)
			throws ServletException, IOException {
		int rows = 6;
		String path = VIEWS + JSP_LIST;
		try {
			CardDAO dao = new CardDAO();
			SessionAuthInfo info = getSessionAuthInfo(req);
			//����¡ ���� ó��
			Pager pager = new Pager();
			String page = req.getParameter(PARAM_PAGE);
			int currentPage = page!=null&&page.length()>0?Integer.parseInt(page):1;
			int dataCount = dao.count(info.getUserNum());
			System.out.println(dataCount+"�� ����");
			int totalPage = pager.pageCount(rows, dataCount);
			int[] pages = pager.paging(rows, currentPage, totalPage);
			//����¡ ���� attributes ����
			System.out.println(pager.getOffset(currentPage, rows) + "������ ����");
			System.out.println(currentPage + "/" + totalPage + ">" + pager.getOffset(currentPage, rows) );
			setPagerAttributes(dataCount, currentPage, totalPage, pages, apiPath + API_LIST, "", attributes);
			List<CardDTO> list = dao.listCard(info.getUserNum(), pager.getOffset(currentPage, rows),rows);
			attributes.put(ATTRIBUTE_LIST, list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		forward(req, resp, path, attributes);
	}

	protected void detail(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> attributes)
			throws ServletException, IOException {
		String path = VIEWS + JSP_DETAIL;
		String tab = req.getParameter(PARAM_TAB);
		CardDAO dao = new CardDAO();
		CardChargeDAO chargeDAO = new CardChargeDAO();
		OrderDAO orderDAO = new OrderDAO();
		CardDTO dto;
		SessionAuthInfo info = getSessionAuthInfo(req);
		try {
			int cardNum = Integer.parseInt(req.getParameter(PARAM_CARD_NUM));
			dto = dao.readCard(cardNum, info.getUserNum());
			if (dto == null) {
				throw new Exception("ī�尡 �������� �ʽ��ϴ�. cardNum:" + cardNum);
			}
			List<OrderHistoryDTO> historyList; 
			List<CardChargeDTO> chargeList;
			if(tab==null || tab.equalsIgnoreCase("usage")) {
				historyList = orderDAO.listOrderHistoryByCardNum(cardNum, info.getUserNum());
				attributes.put(ATTRIBUTE_ORDER_HISTORY, historyList);
			}else {
				chargeList = chargeDAO.listCardCharge(cardNum, info.getUserNum());
				attributes.put(ATTRIBUTE_CARD_CHARGE_LIST, chargeList);
			}
			attributes.put(PARAM_TAB, tab);
			attributes.put(ATTRIBUTE_CARD_DTO, dto);
			forward(req, resp, path, attributes);
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(apiPath + API_LIST);
			return;
		}
	}

	protected void register(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> attributes)
			throws ServletException, IOException {
		String paramStep = req.getParameter(PARAM_REGISTER_STEP);
		try {
			int step = 1;
			// ������ �α��� �����ϱ�
			if (paramStep != null) {
				step = Integer.parseInt(paramStep);
			}
			switch (step) {
			default:
			case PARAM_REGISTER_STEP_1:
				registerStep1(req, resp, attributes);
				break;
			case PARAM_REGISTER_STEP_2:
				registerStep2(req, resp, attributes);
				break;
			case PARAM_REGISTER_STEP_3:
				registerSubmit(req, resp, attributes);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			registerStep1(req, resp, attributes);
			return;
		}
	}

	protected void registerStep1(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> attributes)
			throws ServletException, IOException {
		final int rows = 12;
		String path = VIEWS + JSP_REGISTER_STEP1;
		// ī��� ���� ������
		CardModelDAO dao = new CardModelDAO();
		try {
			//����¡ ���� ó��
			Pager pager = new Pager();
			String page = req.getParameter(PARAM_PAGE);
			int currentPage = page!=null&&page.length()>0?Integer.parseInt(page):1;
			int dataCount = dao.count();
			int totalPage = pager.pageCount(rows, dataCount);
			int[] pages = pager.paging(rows, currentPage, totalPage);
			List<CardModelDTO> list = dao.listCardModel(pager.getOffset(currentPage, rows), rows);
			//����¡ ���� attributes ����
			setPagerAttributes(dataCount, currentPage, totalPage, pages, apiPath + API_REGISTER, "step=" + PARAM_REGISTER_STEP_1, attributes);
			attributes.put(PARAM_MODE, PARAM_MODE_REGISTER);
			attributes.put(ATTRIBUTE_LIST, list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		forward(req, resp, path, attributes);
	}

	protected void registerStep2(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> attributes)
			throws ServletException, IOException {
		String path = VIEWS + JSP_CHARGE;
		try {
			int modelNum = Integer.parseInt(req.getParameter(PARAM_MODEL_NUM));
			CardModelDAO modelDAO = new CardModelDAO();
			CardDTO cardDTO = new CardDTO();
			cardDTO.setModelNum(modelNum);
			cardDTO.setCardName("");
			CardModelDTO modelDTO = modelDAO.readCardModel(modelNum);
			attributes.put(PARAM_MODE, PARAM_MODE_REGISTER);
			attributes.put(ATTRIBUTE_CARD_DTO, cardDTO);
			attributes.put(ATTRIBUTE_CARD_MODEL_DTO, modelDTO);
			forward(req, resp, path, attributes);
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(apiPath + API_REGISTER);
			return;
		}
	}

	protected void registerSubmit(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> attributes)
			throws ServletException, IOException {
		// ī�� ����ϱ�
		try {
			SessionAuthInfo info = getSessionAuthInfo(req);
			CardDAO cardDAO = new CardDAO();
			CardChargeDAO chargeDAO = new CardChargeDAO();
			// ī�� ���� �Ķ���Ϳ��� �޾ƿ���
			String cardName = req.getParameter(PARAM_CARD_NAME);
			int price = Integer.parseInt(req.getParameter(PARAM_PRICE));
			int modelNum = Integer.parseInt(req.getParameter(PARAM_MODEL_NUM));
			// ī�� �ű� ���
			CardDTO cardDTO = new CardDTO(cardName, info.getUserNum(), modelNum);
			int cardNum = cardDAO.insertCard(cardDTO);
			cardDTO = cardDAO.readCard(cardNum, info.getUserNum());
			// �ű� ����� ī�忡 �����ϱ�
			CardChargeDTO chargeDTO = new CardChargeDTO(cardNum, price);
			chargeDAO.insertCardCharge(chargeDTO);
			// ������ �Ϸ�Ǹ� ������� ���ư���
			resp.sendRedirect(apiPath + API_LIST);
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(apiPath + API_REGISTER);
		}
	}

	protected void updateCardName(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> attributes)
			throws ServletException, IOException {
		SessionAuthInfo info = getSessionAuthInfo(req);
		String uri = req.getRequestURI();
		String cardNum = req.getParameter(PARAM_CARD_NUM);
		String cardName = req.getParameter(PARAM_CARD_NAME);
		try {
			CardDAO dao = new CardDAO();
			int cNum = Integer.parseInt(cardNum);
			dao.updateCardName(info.getUserNum(), cNum, cardName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		resp.sendRedirect(apiPath + API_DETAIL + "?" + PARAM_CARD_NUM + "=" + cardNum);
	}
	
	protected void chargeForm(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> attributes)
			throws ServletException, IOException {
		SessionAuthInfo info = getSessionAuthInfo(req);
		String path = VIEWS + JSP_CHARGE;
		attributes.put(PARAM_MODE, PARAM_MODE_CHARGE);
		CardDAO dao = new CardDAO();
		CardDTO dto = null;
		try {
			String cardNum = req.getParameter(PARAM_CARD_NUM);
			if (cardNum == null) {
				dto = dao.readRecentCard(info.getUserNum());
			} else {
				dto = dao.readCard(Integer.parseInt(cardNum), info.getUserNum());
			}
			if (dto == null) {
				throw new Exception("ī�尡 �������� �ʽ��ϴ�.");
			}
			attributes.put(ATTRIBUTE_CARD_DTO, dto);
			forward(req, resp, path, attributes);
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(apiPath + API_DETAIL);
		}
	}

	protected void chargeSubmit(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> attributes) {
		// ���� ���� ����
		try {
			SessionAuthInfo info = getSessionAuthInfo(req);
			int cardNum = Integer.parseInt(req.getParameter(PARAM_CARD_NUM));
			int price = Integer.parseInt(req.getParameter(PARAM_PRICE));
			CardDAO cardDAO = new CardDAO();
			CardDTO cardDTO = cardDAO.readCard(cardNum, info.getUserNum());
			if (cardDTO == null) {
				throw new Exception("ī�尡 �������� �ʽ��ϴ�.");
			}
			if (cardDTO.getBalance() + price > MAX_BALANCE) {
				throw new ChargeException("ī�� �����ݾ��� 550,000���� �ѱ� �� �����ϴ�.");
			}
			CardChargeDAO chargeDAO = new CardChargeDAO();
			chargeDAO.insertCardCharge(new CardChargeDTO(cardNum, price));
			resp.sendRedirect(apiPath + API_DETAIL + "?" + PARAM_CARD_NUM + "=" + cardNum);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				resp.sendRedirect(apiPath + API_LIST);
			} catch (IOException e1) {
			}
		}
	}

	protected void closeForm(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> attributes)
			throws ServletException, IOException {
		SessionAuthInfo info = getSessionAuthInfo(req);
		String path = VIEWS + JSP_CHARGE;
		attributes.put(PARAM_MODE, PARAM_MODE_CLOSE);
		String cardNum = req.getParameter(PARAM_CARD_NUM);
		CardDAO dao = new CardDAO();
		try {
			// ���� ī�� ���� �Ʊ�
			attributes.put(ATTRIBUTE_CARD_DTO, dao.readCard(Integer.parseInt(cardNum), info.getUserNum()));
			// ��ü�� ī�� ���� �Ʊ�
			List<CardDTO> list = dao.listCard(info.getUserNum());
			for (int i = 0; i < list.size(); i++) {
				// ���� ī�� ���� ����
				if (list.get(i).getCardNum() == Integer.parseInt(cardNum)) {
					list.remove(i);
					break;
				}
			}
			attributes.put(ATTRIBUTE_LIST, list);
			forward(req, resp, path, attributes);
		} catch (Exception e) {
			resp.sendRedirect(apiPath + API_DETAIL + "?" + PARAM_CARD_NUM + "=" + cardNum);
			return;
		}
	}

	protected void closeSubmit(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> attributes)
			throws ServletException, IOException {
		// ������ ī���ȣ(cardNum), ��ü�� ī��(targetCardNum)
		// ������ ���ؼ� ��ü�ϴ� ��쿡�� 55������ �ʰ��ϴ� ���� �������!
		System.out.println("�޼��� ����");
		CardDAO dao = new CardDAO();
		String closeCardNum = req.getParameter(PARAM_CARD_NUM);
		String targetCardNum = req.getParameter(PARAM_TARGET_CARD_NUM);
		try {
			System.out.println(closeCardNum + ", " + targetCardNum + "�õ�");
			int closeNum = Integer.parseInt(closeCardNum);
			int targetNum = Integer.parseInt(targetCardNum);
			dao.closeCard(closeNum, targetNum);
			resp.sendRedirect(apiPath + API_LIST);
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(apiPath + API_DETAIL + "?" + PARAM_CARD_NUM + "=" + closeCardNum);
		}

	}

	protected void order(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> attributes)
			throws ServletException, IOException {
		String path = VIEWS + JSP_ORDER;
		String menuNum = req.getParameter(PARAM_MENU_NUM);
		attributes.put(ATTRIBUTE_MAX_ITEM_AMOUNT, MAX_ITEM_AMOUNT);
		try {
			SessionCart cart = getCart(req);
			// #1. �޴� ����Ʈ �̱�
			MenuDAO menuDAO = new MenuDAO();
			List<MenuDTO> list = menuDAO.listAllMenu(0, 100);
			// #2. ���� ��ٱ��� �������� ����� �δ� �� ���� ��..
			if(menuNum!=null && menuNum.length()>0) {
				int mNum = Integer.parseInt(menuNum);
				MenuDTO dto = menuDAO.readMenu(mNum); //TODO: �޼���� �����ϱ�
				if(dto!=null) {
					//TODO: ���ǿ� �ֹ����� �߰��ϱ�
				}
			}
			boolean isAdded = addCart(menuNum, cart, menuDAO);
			if (isAdded == false && menuNum != null && cart.getTotalQuantity()>MAX_ITEM_AMOUNT) {
				attributes.put(ATTRIBUTE_ERROR_MSG, new ErrorMessage("���� ������ �ʰ��Ͽ����ϴ�.", "�ֹ��� �� ���� 15�������� ���Ű� �����մϴ�."));
			}
			attributes.put(ATTRIBUTE_LIST, list);
			attributes.put(SESSION_CART, cart);
			forward(req, resp, path, attributes);
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(apiPath + API_LIST);
			return;
		}
	}

	private boolean addCart(String menuNum, SessionCart cart, MenuDAO menuDAO) {
		// īƮ�� �߰��Ǹ� true/ �� �Ǹ� false�� ��ȯ��
		try {
			if (menuNum != null && menuNum.length() > 0) {
				if (cart.getItems().size() < MAX_ITEM_AMOUNT) {
					// 20�� �̸��� īƮ�� ���� �� ����
					int mNum = Integer.parseInt(menuNum);
					MenuDTO dto = getCartItem(mNum, cart);
					if (dto != null) {
						// īƮ�� ������ īƮ���� ��ü �����ϱ�
						dto.setQuantity(dto.getQuantity()+1);//���� ���ϱ�
					} else {
						// ī�忡 ������ DB���� �ҷ�����
						dto = menuDAO.readMenu(mNum); // TODO: �޼���� �����ϱ�
						if (dto != null) {
							cart.addItem(dto);
						}
						return true;
					}
				} else {// 20���� �ʰ��� ��� �޽��� ����ϱ�
						// TODO: �ڵ� �ۼ�
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	protected void buyForm(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> attributes)
			throws ServletException, IOException {
		String path = VIEWS + JSP_ORDER;
		try {
			// #0. �߰��Ǵ� �Ķ���� ������ �޾Ƽ� �߰� ���� �ϱ�
			SessionAuthInfo info = getSessionAuthInfo(req);
			MenuDAO menuDAO = new MenuDAO();
			CardDAO cardDAO = new CardDAO();
			String menuNum = req.getParameter(PARAM_MENU_NUM);
			if (menuNum != null && menuNum.length() > 0) {
				addCart(menuNum, getCart(req), menuDAO);
			}
			// #1. �޴� ����Ʈ �̱�
			List<MenuDTO> list = menuDAO.listAllMenu(0, 100);
			// #2. �������� ���� ���� ī�� ��� �̱�
			List<CardDTO> cards = cardDAO.listCard(info.getUserNum());
			// ��κ��� ���θ��� �󸶳� �������� �� �����ֳ�
			attributes.put(ATTRIBUTE_LIST, list);
			attributes.put(ATTRIBUTE_CARDS, cards);
			forward(req, resp, path, attributes);
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(apiPath + API_LIST);
			return;
		}
	}

	protected void buySubmit(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> attributes)
			throws ServletException, IOException {
//		String path = apiPath + API_ORDER;
		try {
			// TODO: ����
			// īƮ ������� ���� ����
			SessionAuthInfo info = getSessionAuthInfo(req);
			int cardNum = Integer.parseInt(req.getParameter(PARAM_CARD_NUM));
			SessionCart cart = getCart(req);
			OrderDAO dao = new OrderDAO();
			dao.addOrderHistory(cart, info.getUserNum(), cardNum);//storenum ���� ������ �ǹ� ����
			//������ ������ �Ϸ�Ǿ����� īƮ�� ����.
			clearCart(req);
			resp.sendRedirect(apiPath + API_ORDERED_LIST);
		} catch (OrderException e) {
			//�ֹ��� ������ ���
			e.printStackTrace();
			resp.sendRedirect(apiPath + API_ORDER);
			return;
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(apiPath + API_LIST);
			return;
		}
	}

	protected void orderedList(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> attributes)
			throws ServletException, IOException {
		String path = VIEWS + JSP_ORDER;
		final int rows = 10;
		try {
			SessionAuthInfo info = getSessionAuthInfo(req);
			OrderDAO dao = new OrderDAO();
			//����¡ ���� ó��
			Pager pager = new Pager();
			String page = req.getParameter(PARAM_PAGE);
			int currentPage = page!=null&&page.length()>0?Integer.parseInt(page):1;
			int dataCount = dao.orderCountByUserNum(info.getUserNum());
			int totalPage = pager.pageCount(rows, dataCount);
			int[] pages = pager.paging(rows, currentPage, totalPage);
			//����¡ ���� attributes ����
			setPagerAttributes(dataCount, currentPage, totalPage, pages, apiPath + API_ORDERED_LIST, "", attributes);
			//DB���� �ҷ�����
			List<OrderHistoryDTO> orderHistory = dao.listOrderHistoryByUserNum(info.getUserNum(),  pager.getOffset(currentPage, rows), rows);
			attributes.put(ATTRIBUTE_ORDER_HISTORY, orderHistory);
			forward(req, resp, path, attributes);
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(apiPath + API_LIST);
			return;
		}
	}

	//////////////////////
	protected void forward(HttpServletRequest req, HttpServletResponse resp, String path,
			Map<String, Object> attributes) throws ServletException, IOException {
		setAttributes(req, attributes);
		super.forward(req, resp, path);
	}

	private void setAttributes(HttpServletRequest req, Map<String, Object> attributes) {
		if (req == null || attributes == null) {
			return;
		}
		for (String key : attributes.keySet()) {
			Object value = attributes.getOrDefault(key, "");
//			Ư�� �׸� ���ڵ�
//			if(value != null && req.getMethod().equalsIgnoreCase("GET") && value instanceof String) {
//				if(key.equals("Ư���׸�")){
//					try {
//						value = (Object)URLDecoder.decode(((String)value),"utf-8");
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
			req.setAttribute(key, value);
		}
	}

	private void goToLogin(HttpServletResponse resp) {
		try {
			resp.sendRedirect(contextPath + "/auth/login.do");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private MenuDTO getCartItem(int menuNum, SessionCart cart) throws Exception {
		try {
			return cart.getItems().get(menuNum);
//			List<MenuDTO> items = cart.getItems();
//			for (MenuDTO dto : items) {
//				if (menuNum == dto.getMenuNum()) {
////					MenuDTO newDTO = new MenuDTO();
////					newDTO.setMenuNum(dto.getMenuNum());
////					newDTO.setCategoryNum(dto.getCategoryNum());
////					newDTO.setMenuName(dto.getMenuName());
////					newDTO.setThumbnail(dto.getThumbnail());
////					newDTO.setText(dto.getText());
////					newDTO.setPrice(dto.getPrice());
////					return newDTO;
//					return dto;
//				}
//			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private SessionCart getCart(HttpServletRequest req) throws Exception {
		try {
			SessionCart cart = (SessionCart) req.getSession().getAttribute(SESSION_CART);
			if (cart == null) {
				cart = new SessionCart();
				req.getSession().setAttribute(SESSION_CART, cart);
			}
			return cart;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
	
	private void clearCart(HttpServletRequest req) throws Exception{
		try {
			req.getSession().setAttribute(SESSION_CART, null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
	}
}
