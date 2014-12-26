package ru.acti.utils.interceptors;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.*;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.springframework.stereotype.Component;
import ru.acti.entity.ErrorCodeEnum;
import ru.acti.entity.ResponseObject;
import ru.acti.entity.ResponseStatus;
import ru.acti.entity.SmsServiceException;

import java.util.ArrayList;
import java.util.List;

@Component
public class ResponseOutFaultInterceptor extends AbstractOutDatabindingInterceptor {

    public ResponseOutFaultInterceptor() {
        super(Phase.SETUP);
    }

    @Override
    public void handleMessage(Message message) throws Fault {

        Exception exception = message.getContent(Exception.class);
        Exchange exchange = message.getExchange();

        resetOrigInterceptorChain(message);
        resetFault(exchange);

        Message outMessage = createOutMessage(exchange, exception);

        InterceptorChain chain = prepareNewInterceptorChain(exchange);
        chain.doIntercept(outMessage);

    }

    private List fillResponseObjectList(Exception exception) {

        ResponseObject responseObject = new ResponseObject(ResponseStatus.ERROR);

        if (exception != null) {

            if (exception.getCause() instanceof SmsServiceException) {
                responseObject.setCode(((SmsServiceException) exception.getCause()).getErrorCodeEnum());
            } else {
                responseObject.setCode(ErrorCodeEnum.FATAl_ERROR);
            }

            responseObject.setMessage(exception.getMessage());
        }

        List result = new ArrayList();
        result.add(responseObject);

        return result;
    }

    private InterceptorChain prepareNewInterceptorChain(Exchange exchange) {
        Message message = exchange.getOutMessage();

        InterceptorChain chain = OutgoingChainInterceptor.getOutInterceptorChain(exchange);
        message.setInterceptorChain(chain);

        return chain;
    }

    private Message createOutMessage(Exchange exchange, Exception exception) {
        Endpoint ep = exchange.get(Endpoint.class);

        Message outMessage = ep.getBinding().createMessage();
        outMessage.setExchange(exchange);
        outMessage.setContent(List.class, fillResponseObjectList(exception));

        exchange.setOutMessage(outMessage);
        return outMessage;
    }

    private void resetFault(Exchange exchange) {
        exchange.put(Exception.class, null);
    }

    private void resetOrigInterceptorChain(Message message) {
        InterceptorChain chain = message.getInterceptorChain();
        for (Interceptor<?> interceptor : chain) {
            chain.remove(interceptor);
        }
        chain.reset();
    }

}
